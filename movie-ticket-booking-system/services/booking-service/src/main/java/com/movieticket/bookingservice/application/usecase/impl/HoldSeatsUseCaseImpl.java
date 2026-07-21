package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.HoldSeatsResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;
import com.movieticket.bookingservice.application.usecase.HoldSeatsUseCase;
import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.entity.SeatHoldSeat;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import com.movieticket.bookingservice.domain.port.BookingEventOutboxRepository;
import com.movieticket.bookingservice.domain.port.BookingSettingRepository;
import com.movieticket.bookingservice.domain.port.SeatHoldRepository;
import com.movieticket.bookingservice.domain.port.TicketRepository;
import com.movieticket.bookingservice.domain.vo.HoldToken;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldSeatsUseCaseImpl implements HoldSeatsUseCase {

    private final SeatHoldRepository seatHoldRepository;
    private final TicketRepository ticketRepository;
    private final BookingEventOutboxRepository outboxRepository;
    private final BookingSettingRepository bookingSettingRepository;
    private final RedissonClient redissonClient;
    private final ShowtimeClient showtimeClient;
    private final CinemaClient cinemaClient;

    private static final String SETTING_HOLD_TTL = "seat_hold_ttl_minutes";
    private static final String SETTING_MAX_SEATS = "max_seats_per_hold";
    private static final String SETTING_LOCK_WAIT = "lock_wait_time_seconds";
    private static final String SETTING_LOCK_LEASE = "lock_lease_time_seconds";
    private static final String LOCK_KEY_PREFIX = "lock:showtime:%d:seat:%s";

    private static final int DEFAULT_HOLD_TTL = 5;
    private static final int DEFAULT_MAX_SEATS = 8;
    private static final int DEFAULT_LOCK_WAIT = 2;
    private static final int DEFAULT_LOCK_LEASE = 10;

    @Override
    @Transactional
    public HoldSeatsResponse execute(HoldSeatsCommand command) {
        Long showtimeId = command.getShowtimeId();
        List<String> seatCodes = command.getSeatCodes();
        Long userId = command.getUserId();
        LocalDateTime now = LocalDateTime.now();

        int maxSeats = getIntSetting(SETTING_MAX_SEATS, DEFAULT_MAX_SEATS);
        if (seatCodes.size() > maxSeats) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Maximum " + maxSeats + " seats per hold request");
        }

        Set<String> uniqueSeats = new HashSet<>(seatCodes);
        if (uniqueSeats.size() != seatCodes.size()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Duplicate seat codes in request");
        }

        List<String> sortedSeats = seatCodes.stream()
                .map(String::trim)
                .sorted()
                .collect(Collectors.toList());

        validateShowtime(showtimeId);

        int lockWait = getIntSetting(SETTING_LOCK_WAIT, DEFAULT_LOCK_WAIT);
        int lockLease = getIntSetting(SETTING_LOCK_LEASE, DEFAULT_LOCK_LEASE);

        List<RLock> acquiredLocks = new ArrayList<>();
        try {
            for (String seatCode : sortedSeats) {
                String lockKey = String.format(LOCK_KEY_PREFIX, showtimeId, seatCode);
                RLock lock = redissonClient.getLock(lockKey);
                boolean acquired = lock.tryLock(lockWait, lockLease, TimeUnit.SECONDS);
                if (!acquired) {
                    log.warn("Failed to acquire lock for seat {} (showtime {})", seatCode, showtimeId);
                    throw new ApiException(ErrorCode.SEAT_ALREADY_HELD,
                            "Seat " + seatCode + " is currently being held by another user");
                }
                acquiredLocks.add(lock);
                log.debug("Acquired lock for seat {} (showtime {})", seatCode, showtimeId);
            }

            for (String seatCode : sortedSeats) {
                if (seatHoldRepository.existsActiveHoldForSeat(showtimeId, seatCode, now)) {
                    throw new ApiException(ErrorCode.SEAT_ALREADY_HELD,
                            "Seat " + seatCode + " is already held by another user");
                }
                if (ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(showtimeId, seatCode,
                        List.of(TicketStatus.ACTIVE, TicketStatus.USED))) {
                    throw new ApiException(ErrorCode.SEAT_UNAVAILABLE,
                            "Seat " + seatCode + " is already booked");
                }
            }

            int holdTtl = getIntSetting(SETTING_HOLD_TTL, DEFAULT_HOLD_TTL);
            HoldToken holdToken = HoldToken.generate();
            LocalDateTime expiresAt = now.plusMinutes(holdTtl);

            List<SeatHoldSeat> holdSeats = sortedSeats.stream()
                    .map(code -> SeatHoldSeat.builder()
                            .showtimeId(showtimeId)
                            .seatCode(code)
                            .createdAt(now)
                            .build())
                    .collect(Collectors.toList());

            SeatHold seatHold = SeatHold.builder()
                    .holdToken(holdToken.value())
                    .userId(userId)
                    .showtimeId(showtimeId)
                    .status(SeatHoldStatus.ACTIVE)
                    .expiresAt(expiresAt)
                    .createdAt(now)
                    .updatedAt(now)
                    .seats(holdSeats)
                    .build();

            seatHoldRepository.save(seatHold);

            BookingEventOutbox outbox = BookingEventOutbox.builder()
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType("SeatHold")
                    .aggregateId(holdToken.value())
                    .bookingId(null)
                    .eventType("SEAT_HOLD_CREATED")
                    .topic("booking.seat-hold.created")
                    .payloadJson("{\"holdToken\":\"" + holdToken.value()
                            + "\",\"userId\":" + userId
                            + ",\"showtimeId\":" + showtimeId
                            + ",\"expiresAt\":\"" + expiresAt + "\"}")
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxRepository.save(outbox);

            log.info("Seats held successfully: token={}, seats={}, expiresAt={}",
                    holdToken.value(), sortedSeats, expiresAt);

            List<HoldSeatsResponse.SeatHoldSeatDto> seatDtos = sortedSeats.stream()
                    .map(code -> HoldSeatsResponse.SeatHoldSeatDto.builder().seatCode(code).build())
                    .collect(Collectors.toList());

            return HoldSeatsResponse.builder()
                    .holdToken(holdToken.value())
                    .expiresAt(expiresAt)
                    .seats(seatDtos)
                    .build();

        } catch (ApiException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Lock acquisition interrupted");
        } catch (Exception e) {
            log.error("Error holding seats: {}", e.getMessage(), e);
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Failed to hold seats: " + e.getMessage());
        } finally {
            for (RLock lock : acquiredLocks) {
                try {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                        log.debug("Released lock: {}", lock.getName());
                    }
                } catch (Exception e) {
                    log.warn("Error releasing lock {}: {}", lock.getName(), e.getMessage());
                }
            }
        }
    }

    private void validateShowtime(Long showtimeId) {
        try {
            Map<String, Object> showtime = showtimeClient.getShowtime(showtimeId);
            if (showtime == null || showtime.isEmpty()) {
                throw new ApiException(ErrorCode.SHOWTIME_NOT_FOUND,
                        "Showtime not found: " + showtimeId);
            }
            log.debug("Validated showtime {}: {}", showtimeId, showtime);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not validate showtime {} via Feign: {}", showtimeId, e.getMessage());
        }
    }

    private int getIntSetting(String key, int defaultValue) {
        try {
            return bookingSettingRepository.findBySettingKey(key)
                    .map(setting -> {
                        try {
                            return Integer.parseInt(setting.getSettingValue());
                        } catch (NumberFormatException e) {
                            return defaultValue;
                        }
                    })
                    .orElse(defaultValue);
        } catch (Exception e) {
            log.warn("Could not read setting {}, using default {}: {}", key, defaultValue, e.getMessage());
            return defaultValue;
        }
    }
}
