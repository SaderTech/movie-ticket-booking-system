package com.userservice.domain.valueobject;

public record PhoneNumber(String value) {

    public PhoneNumber {
        if (value == null || !value.matches("\\d{10,11}")) {
            throw new IllegalArgumentException("Invalid phone number");
        }
    }
}