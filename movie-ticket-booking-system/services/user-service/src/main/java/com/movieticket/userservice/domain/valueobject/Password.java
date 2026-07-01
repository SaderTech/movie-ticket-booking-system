package com.movieticket.userservice.domain.valueobject;

public record Password(String value) {

    public Password {
        if (value == null || value.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }
}