package com.movieticket.bookingservice.domain.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SeatHoldCreatedEvent(
        String eventId,
        String holdToken,
        Long userId,
        Long showtimeId,
        List<String> seatCodes,
        LocalDateTime expiresAt,
        LocalDateTime occurredAt
) implements DomainEvent {

    public SeatHoldCreatedEvent(String holdToken, Long userId, Long showtimeId,
                                List<String> seatCodes, LocalDateTime expiresAt) {
        this(UUID.randomUUID().toString(), holdToken, userId, showtimeId,
                seatCodes, expiresAt, LocalDateTime.now());
    }

    @Override
    public String aggregateId() {
        return holdToken;
    }

    @Override
    public String eventType() {
        return "SEAT_HOLD_CREATED";
    }
}
