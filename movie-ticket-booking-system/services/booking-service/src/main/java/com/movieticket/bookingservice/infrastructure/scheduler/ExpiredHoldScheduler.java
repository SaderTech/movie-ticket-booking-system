package com.movieticket.bookingservice.infrastructure.scheduler;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.entity.SagaTransaction;
import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.enums.BookingStatus;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import com.movieticket.bookingservice.domain.event.BookingCancelledEvent;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingEventOutboxRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSagaTransactionRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSeatHoldRepository;
import com.movieticket.bookingservice.infrastructure.publisher.DomainEventPublisherImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredHoldScheduler {

    private final JpaSeatHoldRepository seatHoldRepository;
    private final JpaBookingEventOutboxRepository outboxRepository;
    private final JpaBookingRepository bookingRepository;
    private final JpaSagaTransactionRepository sagaTransactionRepository;
    private final DomainEventPublisherImpl domainEventPublisher;
    private final RedissonClient redissonClient;

    @Lazy
    @Autowired
    private ExpiredHoldScheduler self;

    private static final int DEFAULT_LOCK_WAIT = 2;
    private static final int DEFAULT_LOCK_LEASE = 10;

    @Scheduled(fixedRate = 60000)
    public void expireStaleHolds() {
        RLock lock = redissonClient.getLock("scheduler:expired-hold");
        if (lock == null) {
            log.warn("Redisson lock for expired-hold scheduler is unavailable, skipping run");
            return;
        }
        try {
            if (!lock.tryLock(DEFAULT_LOCK_WAIT, DEFAULT_LOCK_LEASE, TimeUnit.SECONDS)) {
                log.info("Expired hold processing already running on another instance, skipping");
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted for expired hold scheduler");
            return;
        }
        try {
            self.doExpireStaleHolds();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                try { lock.unlock(); } catch (Exception e) { log.warn("Error releasing scheduler lock: {}", e.getMessage()); }
            }
        }
    }

    protected void doExpireStaleHolds() {
        List<SeatHold> expiredHolds = seatHoldRepository.findByStatusAndExpiresAtBefore(SeatHoldStatus.ACTIVE, LocalDateTime.now());
        if (expiredHolds.isEmpty()) {
            return;
        }

        log.info("Expiring {} stale seat holds", expiredHolds.size());
        for (SeatHold hold : expiredHolds) {
            self.processExpiredHold(hold.getId());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processExpiredHold(Long holdId) {
        SeatHold hold = seatHoldRepository.findById(holdId).orElse(null);
        if (hold == null) {
            log.warn("Seat hold {} no longer exists, skipping expiration", holdId);
            return;
        }
        if (hold.getStatus() != SeatHoldStatus.ACTIVE
                || hold.getExpiresAt() == null
                || !hold.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.debug("Seat hold {} is no longer stale and active, skipping expiration", holdId);
            return;
        }

        hold.expire();
        seatHoldRepository.save(hold);

        bookingRepository.findByHoldToken(hold.getHoldToken())
                .filter(booking -> booking.getStatus() == BookingStatus.PENDING_PAYMENT)
                .ifPresent(booking -> expirePendingBooking(booking, hold));

        String seatCodesJson = hold.getSeats().stream()
                .map(s -> "\"" + s.getSeatCode() + "\"")
                .collect(Collectors.joining(",", "[", "]"));

        BookingEventOutbox outbox = BookingEventOutbox.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType("SeatHold")
                .aggregateId(hold.getHoldToken())
                .eventType("SEAT_HOLD_EXPIRED")
                .topic("booking.seat-hold.expired")
                .payloadJson("{\"holdToken\":\"" + hold.getHoldToken()
                        + "\",\"userId\":" + hold.getUserId()
                        + ",\"showtimeId\":" + hold.getShowtimeId()
                        + ",\"seatCodes\":" + seatCodesJson + "}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
        outboxRepository.save(outbox);
    }

    private void expirePendingBooking(Booking booking, SeatHold hold) {
        String reason = "Seat hold expired before payment was completed";
        booking.fail(reason);
        bookingRepository.save(booking);

        sagaTransactionRepository.findByBookingId(booking.getId()).ifPresent(saga -> {
            saga.startCompensation();
            saga.compensate();
            sagaTransactionRepository.save(saga);
        });

        domainEventPublisher.publish(new BookingCancelledEvent(
                booking.getBookingCode(), booking.getUserId(), reason));
        log.info("Cancelled pending booking {} after hold {} expired", booking.getBookingCode(), hold.getHoldToken());
    }
}
