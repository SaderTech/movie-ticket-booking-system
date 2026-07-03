package com.movieticket.bookingservice.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingCancelledEvent(
        String eventId,
        String bookingCode,
        String reason,
        LocalDateTime occurredAt
) implements DomainEvent {

    public BookingCancelledEvent(String bookingCode, String reason) {
        this(UUID.randomUUID().toString(), bookingCode, reason, LocalDateTime.now());
    }

    @Override
    public String aggregateId() {
        return bookingCode;
    }

    @Override
    public String eventType() {
        return "BOOKING_CANCELLED";
    }
}
