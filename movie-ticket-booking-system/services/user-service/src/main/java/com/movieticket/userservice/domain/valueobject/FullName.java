package com.movieticket.userservice.domain.valueobject;
import java.util.Objects;

public record FullName(String value) {

    public FullName {

        Objects.requireNonNull(value);

        value = value.trim();

        if (value.length() < 2 || value.length() > 100) {
            throw new IllegalArgumentException("Invalid full name");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}