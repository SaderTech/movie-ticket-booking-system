package com.movieticket.userservice.domain.valueobject;

import java.util.Objects;

public record Password(String value) {

    public Password {

        Objects.requireNonNull(value);

        value = value.trim();

        if (value.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        if (value.length() > 100) {
            throw new IllegalArgumentException("Password too long");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}