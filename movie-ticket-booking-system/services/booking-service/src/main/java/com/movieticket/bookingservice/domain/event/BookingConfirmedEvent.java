package com.movieticket.bookingservice.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingConfirmedEvent(
        String eventId,
        String bookingCode,
        Long userId,
        Long showtimeId,
        LocalDateTime occurredAt
) implements DomainEvent {

    public BookingConfirmedEvent(String bookingCode, Long userId, Long showtimeId) {
        this(UUID.randomUUID().toString(), bookingCode, userId, showtimeId, LocalDateTime.now());
    }

    @Override
    public String aggregateId() {
        return bookingCode;
    }

    @Override
    public String eventType() {
        return "BOOKING_CONFIRMED";
    }
}
