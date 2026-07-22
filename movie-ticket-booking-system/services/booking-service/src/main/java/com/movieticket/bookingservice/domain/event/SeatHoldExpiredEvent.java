package com.movieticket.bookingservice.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SeatHoldExpiredEvent(
        String eventId,
        String holdToken,
        Long userId,
        Long showtimeId,
        List<String> seatCodes,
        LocalDateTime occurredAt
) implements DomainEvent {

    public SeatHoldExpiredEvent(String holdToken, Long userId, Long showtimeId, List<String> seatCodes) {
        this(UUID.randomUUID().toString(), holdToken, userId, showtimeId, seatCodes, LocalDateTime.now());
    }

    @Override
    public String aggregateId() {
        return holdToken;
    }

    @Override
    public String eventType() {
        return "SEAT_HOLD_EXPIRED";
    }
}