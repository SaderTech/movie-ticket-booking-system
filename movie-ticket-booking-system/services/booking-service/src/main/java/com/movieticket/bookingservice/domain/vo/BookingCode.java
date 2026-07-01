package com.movieticket.bookingservice.domain.vo;

import java.util.UUID;

public record BookingCode(String value) {

    private static final String PREFIX = "BK";

    public BookingCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Booking code must not be blank");
        }
        if (!value.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Booking code must start with " + PREFIX);
        }
    }

    public static BookingCode generate() {
        String code = PREFIX + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 12).toUpperCase();
        return new BookingCode(code);
    }
}
