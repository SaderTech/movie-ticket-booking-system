package com.movieticket.bookingservice.domain.vo;

import java.util.UUID;

public record HoldToken(String value) {

    private static final String PREFIX = "HOLD_";

    public HoldToken {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Hold token must not be blank");
        }
    }

    public static HoldToken generate() {
        return new HoldToken(PREFIX + UUID.randomUUID().toString()
                .replace("-", "").substring(0, 8).toUpperCase());
    }
}
