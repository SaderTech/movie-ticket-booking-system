package com.movieticket.userservice.domain.valueobject;

import java.util.Objects;

public record Email(String value) {

    public Email {

        Objects.requireNonNull(value);

        value = value.trim().toLowerCase();

        if (!value.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Invalid email");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}