package com.movieticket.userservice.domain.valueobject;

public record FullName(String value) {

    public FullName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be empty");
        }
    }
}