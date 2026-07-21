package com.movieticket.bookingservice.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record SeatHoldExpiredEvent(
        String eventId,
        String holdToken,
        Long showtimeId,
        LocalDateTime occurredAt
) implements DomainEvent {

    public SeatHoldExpiredEvent(String holdToken, Long showtimeId) {
        this(UUID.randomUUID().toString(), holdToken, showtimeId, LocalDateTime.now());
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
