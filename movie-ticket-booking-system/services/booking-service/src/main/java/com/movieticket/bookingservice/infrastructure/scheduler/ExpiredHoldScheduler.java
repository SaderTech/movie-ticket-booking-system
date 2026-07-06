package com.movieticket.bookingservice.infrastructure.scheduler;

import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import com.movieticket.bookingservice.domain.port.BookingEventOutboxRepository;
import com.movieticket.bookingservice.domain.port.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpiredHoldScheduler {

    private final SeatHoldRepository seatHoldRepository;
    private final BookingEventOutboxRepository outboxRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void expireStaleHolds() {
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredHolds(LocalDateTime.now());
        if (expiredHolds.isEmpty()) {
            return;
        }

        log.info("Expiring {} stale seat holds", expiredHolds.size());
        for (SeatHold hold : expiredHolds) {
            hold.expire();
            seatHoldRepository.save(hold);

            List<String> seatCodes = hold.getSeats().stream()
                    .map(s -> s.getSeatCode())
                    .collect(Collectors.toList());

            BookingEventOutbox outbox = BookingEventOutbox.builder()
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType("SeatHold")
                    .aggregateId(hold.getHoldToken())
                    .eventType("SEAT_HOLD_EXPIRED")
                    .topic("booking.seat-hold.expired")
                    .payloadJson("{\"holdToken\":\"" + hold.getHoldToken()
                            + "\",\"userId\":" + hold.getUserId()
                            + ",\"showtimeId\":" + hold.getShowtimeId()
                            + ",\"seatCodes\":" + seatCodes + "}")
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxRepository.save(outbox);
        }
    }
}
