package com.movieticket.bookingservice.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingConfirmedEvent(
        String eventId,
        String bookingCode,
        Long userId,
        Long showtimeId,
        BigDecimal totalAmount,
        String paymentMethod,
        LocalDateTime occurredAt
) implements DomainEvent {

    public BookingConfirmedEvent(String bookingCode, Long userId, Long showtimeId,
                                  BigDecimal totalAmount, String paymentMethod) {
        this(UUID.randomUUID().toString(), bookingCode, userId, showtimeId,
                totalAmount, paymentMethod, LocalDateTime.now());
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