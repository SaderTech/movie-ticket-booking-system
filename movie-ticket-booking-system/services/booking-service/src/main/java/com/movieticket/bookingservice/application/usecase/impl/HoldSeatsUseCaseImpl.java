package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.HoldSeatsResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;
import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.entity.SeatHoldSeat;
import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.bookingservice.domain.entity.IdempotencyRecord;
import com.movieticket.bookingservice.domain.enums.IdempotencyStatus;
import com.movieticket.bookingservice.domain.vo.HoldToken;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.client.SeatClient;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import com.movieticket.bookingservice.infrastructure.client.dto.CinemaResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.MovieResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.SeatResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.ShowtimeResponse;
import com.movieticket.bookingservice.infrastructure.jpa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldSeatsUseCaseImpl {

    private final JpaBookingRepository bookingRepository;
    private final JpaSeatHoldRepository seatHoldRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaBookingEventOutboxRepository outboxRepository;
    private final JpaBookingSettingRepository bookingSettingRepository;
    private final JpaIdempotencyRecordRepository idempotencyRecordRepository;
    private final RedissonClient redissonClient;
    private final ShowtimeClient showtimeClient;
    private final MovieClient movieClient;
    private final CinemaClient cinemaClient;
    private final SeatClient seatClient;
    private final ObjectMapper objectMapper;

    @Lazy
    @Autowired
    private HoldSeatsUseCaseImpl self;

    private static final String SETTING_HOLD_TTL = "seat_hold_ttl_minutes";
    private static final String SETTING_MAX_SEATS = "max_seats_per_hold";
    private static final String SETTING_LOCK_WAIT = "lock_wait_time_seconds";
    private static final String SETTING_LOCK_LEASE = "lock_lease_time_seconds";
    private static final String LOCK_KEY_PREFIX = "lock:showtime:%d:seat:%s";

    private static final int DEFAULT_HOLD_TTL = 3;
    private static final int DEFAULT_MAX_SEATS = 8;
    private static final int DEFAULT_LOCK_WAIT = 2;
    private static final int DEFAULT_LOCK_LEASE = 10;

    public HoldSeatsResponse execute(HoldSeatsCommand command) {
        Long showtimeId = command.getShowtimeId();
        List<String> seatCodes = command.getSeatCodes();
        Long userId = command.getUserId();

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

        String idempotencyKey = command.getIdempotencyKey();
        String requestHash = computeRequestHash(command);
        if (idempotencyKey != null) {
            Optional<HoldSeatsResponse> cached = checkIdempotency(idempotencyKey, requestHash);
            if (cached.isPresent()) {
                return cached.get();
            }
        }

        List<String> sortedSeats = seatCodes.stream()
                .map(String::trim)
                .sorted()
                .collect(Collectors.toList());

        ShowtimeResponse showtimeData = validateShowtime(showtimeId);
        validateMovie(showtimeData);
        validateCinema(showtimeData);
        validateSeatsInHall(showtimeData, sortedSeats);

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

            HoldSeatsResponse response = self.persistHoldInTransaction(idempotencyKey, requestHash, showtimeId, userId, sortedSeats);
            return response;

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

    private Optional<HoldSeatsResponse> checkIdempotency(String idempotencyKey, String requestHash) {
        return idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                .flatMap(rec -> {
                    if (rec.getRequestHash() != null && !rec.getRequestHash().equals(requestHash)) {
                        throw new ApiException(ErrorCode.INVALID_REQUEST, 409,
                                "Idempotency key reused with different request payload");
                    }
                    switch (rec.getStatus()) {
                        case SUCCEEDED -> {
                            try {
                                return Optional.of(objectMapper.readValue(rec.getResponseBody(), HoldSeatsResponse.class));
                            } catch (Exception e) {
                                log.warn("Failed to deserialize cached idempotency response, reprocessing: {}", e.getMessage());
                            }
                        }
                        case PROCESSING -> throw new ApiException(ErrorCode.INVALID_REQUEST, 409, "Request is already being processed");
                        case FAILED -> {}
                    }
                    return Optional.<HoldSeatsResponse>empty();
                });
    }

    private String computeRequestHash(HoldSeatsCommand command) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("showtimeId", command.getShowtimeId());
            data.put("seatCodes", command.getSeatCodes().stream().sorted().collect(Collectors.toList()));
            String json = objectMapper.writeValueAsString(data);
            return sha256(json);
        } catch (Exception e) {
            log.warn("Failed to compute request hash: {}", e.getMessage());
            return null;
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    @Transactional
    protected HoldSeatsResponse persistHoldInTransaction(String idempotencyKey, String requestHash, Long showtimeId, Long userId,
                                                           List<String> sortedSeats) {
        LocalDateTime now = LocalDateTime.now();

        if (idempotencyKey != null) {
            Optional<HoldSeatsResponse> cached = checkIdempotency(idempotencyKey, requestHash);
            if (cached.isPresent()) {
                return cached.get();
            }

            IdempotencyRecord processingRecord = IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .requestHash(requestHash)
                    .operationType("HOLD")
                    .status(IdempotencyStatus.PROCESSING)
                    .expiresAt(now.plusHours(1))
                    .build();
            try {
                idempotencyRecordRepository.saveAndFlush(processingRecord);
            } catch (DataIntegrityViolationException e) {
                Optional<IdempotencyRecord> rival = idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey);
                if (rival.isEmpty()) {
                    throw new ApiException(ErrorCode.INTERNAL_ERROR, "Idempotency invariant violated: DIVE but no record found");
                }
                if (rival.get().getRequestHash() != null && !rival.get().getRequestHash().equals(requestHash)) {
                    throw new ApiException(ErrorCode.INVALID_REQUEST, 409,
                            "Idempotency key reused with different request payload");
                }
                switch (rival.get().getStatus()) {
                    case SUCCEEDED -> {
                        try {
                            return objectMapper.readValue(rival.get().getResponseBody(), HoldSeatsResponse.class);
                        } catch (Exception ex) {
                            log.warn("Failed to deserialize cached idempotency response, reprocessing: {}", ex.getMessage());
                        }
                    }
                    case PROCESSING -> throw new ApiException(ErrorCode.INVALID_REQUEST, 409, "Request is already being processed");
                    case FAILED -> {
                        log.warn("Idempotency key {} has FAILED status, deleting and recreating", idempotencyKey);
                        idempotencyRecordRepository.delete(rival.get());
                        idempotencyRecordRepository.flush();
                        IdempotencyRecord newProcessing = IdempotencyRecord.builder()
                                .idempotencyKey(idempotencyKey)
                                .requestHash(requestHash)
                                .operationType("HOLD")
                                .status(IdempotencyStatus.PROCESSING)
                                .expiresAt(now.plusHours(1))
                                .build();
                        idempotencyRecordRepository.saveAndFlush(newProcessing);
                    }
                }
            }
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
            if (bookingRepository.existsPendingBookingForSeat(showtimeId, seatCode,
                    List.of(BookingSeatStatus.PENDING, BookingSeatStatus.CONFIRMED))) {
                throw new ApiException(ErrorCode.SEAT_ALREADY_BOOKED,
                        "Seat " + seatCode + " has a pending or confirmed booking");
            }
        }

        int holdTtl = getIntSetting(SETTING_HOLD_TTL, DEFAULT_HOLD_TTL);
        HoldToken holdToken = HoldToken.generate();
        LocalDateTime expiresAt = now.plusMinutes(holdTtl);
        Map<String, String> seatTypeMap = fetchSeatTypeMap(showtimeId);

        List<SeatHoldSeat> holdSeats = sortedSeats.stream()
                .map(code -> SeatHoldSeat.builder()
                        .showtimeId(showtimeId)
                        .seatCode(code)
                        .seatType(seatTypeMap.getOrDefault(code, "NORMAL"))
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
        holdSeats.forEach(s -> s.setSeatHold(seatHold));

        seatHoldRepository.save(seatHold);

        String seatHoldPayload;
        try {
            Map<String, Object> seatHoldJson = new HashMap<>();
            seatHoldJson.put("holdToken", holdToken.value());
            seatHoldJson.put("userId", userId);
            seatHoldJson.put("showtimeId", showtimeId);
            seatHoldJson.put("expiresAt", expiresAt.toString());
            seatHoldPayload = objectMapper.writeValueAsString(seatHoldJson);
        } catch (Exception e) {
            seatHoldPayload = "{\"holdToken\":\"" + holdToken.value()
                    + "\",\"userId\":" + userId + "}";
        }

        BookingEventOutbox outbox = BookingEventOutbox.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType("SeatHold")
                .aggregateId(holdToken.value())
                .bookingId(null)
                .eventType("SEAT_HOLD_CREATED")
                .topic("booking.seat-hold.created")
                .payloadJson(seatHoldPayload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
        outboxRepository.save(outbox);

        log.info("Seats held successfully: token={}, seats={}, expiresAt={}",
                holdToken.value(), sortedSeats, expiresAt);

        List<HoldSeatsResponse.SeatHoldSeatDto> seatDtos = sortedSeats.stream()
                .map(code -> HoldSeatsResponse.SeatHoldSeatDto.builder()
                        .seatCode(code)
                        .seatType(seatTypeMap.getOrDefault(code, "NORMAL"))
                        .build())
                .collect(Collectors.toList());

        HoldSeatsResponse response = HoldSeatsResponse.builder()
                .holdToken(holdToken.value())
                .expiresAt(expiresAt)
                .seats(seatDtos)
                .build();

        if (idempotencyKey != null) {
            idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey).ifPresent(rec -> {
                try {
                    rec.succeed(objectMapper.writeValueAsString(response));
                    idempotencyRecordRepository.save(rec);
                } catch (Exception e) {
                    log.warn("Failed to cache idempotency response: {}", e.getMessage());
                }
            });
        }

        return response;
    }

    private ShowtimeResponse validateShowtime(Long showtimeId) {
        try {
            ShowtimeResponse showtime = showtimeClient.getShowtime(showtimeId);
            if (showtime == null) {
                throw new ApiException(ErrorCode.SHOWTIME_NOT_FOUND,
                        "Showtime not found: " + showtimeId);
            }
            String status = showtime.status();
            if (!"AVAILABLE".equals(status)) {
                throw new ApiException(ErrorCode.SHOWTIME_NOT_FOUND,
                        "Showtime " + showtimeId + " is not available (status: " + status + ")");
            }
            log.debug("Validated showtime {}: {}", showtimeId, showtime);
            return showtime;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ApiException(ErrorCode.SHOWTIME_NOT_FOUND,
                    "Could not validate showtime " + showtimeId + ": " + e.getMessage());
        }
    }

    private void validateMovie(ShowtimeResponse showtimeData) {
        try {
            Long movieId = showtimeData.movieId();
            if (movieId == null) {
                throw new ApiException(ErrorCode.SHOWTIME_NOT_FOUND, "Showtime has no associated movie");
            }
            MovieResponse movie = movieClient.getMovie(movieId);
            if (movie == null) {
                throw new ApiException(ErrorCode.INTERNAL_ERROR, "Movie not found: " + movieId);
            }
            log.debug("Validated movie {} for hold request", movieId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not validate movie, proceeding: {}", e.getMessage());
        }
    }

    private void validateCinema(ShowtimeResponse showtimeData) {
        try {
            Long cinemaId = showtimeData.cinemaId();
            if (cinemaId == null) {
                throw new ApiException(ErrorCode.SHOWTIME_NOT_FOUND, "Showtime has no associated cinema");
            }
            CinemaResponse cinema = cinemaClient.getCinema(cinemaId);
            if (cinema == null) {
                throw new ApiException(ErrorCode.CINEMA_NOT_FOUND, "Cinema not found: " + cinemaId);
            }
            log.debug("Validated cinema {} for hold request", cinemaId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not validate cinema, proceeding: {}", e.getMessage());
        }
    }

    private void validateSeatsInHall(ShowtimeResponse showtimeData, List<String> seatCodes) {
        try {
            Long hallId = showtimeData.roomId();
            if (hallId == null) {
                log.warn("No hallId in showtime data, skipping seat layout validation");
                return;
            }
            List<SeatResponse> hallSeats = seatClient.getSeatsByHallId(hallId);
            Set<String> availableSeatCodes = hallSeats.stream()
                    .map(s -> s.rowName() + s.seatNumber())
                    .collect(Collectors.toSet());

            for (String seatCode : seatCodes) {
                if (!availableSeatCodes.contains(seatCode)) {
                    throw new ApiException(ErrorCode.INVALID_REQUEST,
                            "Seat " + seatCode + " does not exist in hall " + hallId);
                }
            }
            log.debug("Validated {} seats in hall {} for hold request", seatCodes.size(), hallId);
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Could not validate seat layout, proceeding: {}", e.getMessage());
        }
    }

    private Map<String, String> fetchSeatTypeMap(Long showtimeId) {
        try {
            ShowtimeResponse showtimeData = showtimeClient.getShowtime(showtimeId);
            Long hallId = showtimeData.roomId();
            if (hallId == null) {
                log.warn("No hallId in showtime data, falling back to NORMAL seat type");
                return Map.of();
            }
            List<SeatResponse> seats = seatClient.getSeatsByHallId(hallId);
            Map<String, String> map = new HashMap<>();
            for (SeatResponse seat : seats) {
                if (seat.rowName() != null && seat.seatNumber() != null && seat.seatTypeCode() != null) {
                    map.put(seat.rowName() + seat.seatNumber(), seat.seatTypeCode());
                }
            }
            log.debug("Fetched seat type map for hall {}: {} entries", hallId, map.size());
            return map;
        } catch (Exception e) {
            log.warn("Could not fetch seat types from cinema service, falling back to NORMAL: {}", e.getMessage());
            return Map.of();
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
