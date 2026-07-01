package com.movieticket.userservice.domain.valueobject;

import java.util.Objects;

public record PhoneNumber(String value) {

    public PhoneNumber {

        Objects.requireNonNull(value);

        value = value.trim();

        if (value.startsWith("+84")) {
            value = "0" + value.substring(3);
        }

        if (!value.matches("^0\\d{9,10}$")) {
            throw new IllegalArgumentException("Invalid phone number");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}