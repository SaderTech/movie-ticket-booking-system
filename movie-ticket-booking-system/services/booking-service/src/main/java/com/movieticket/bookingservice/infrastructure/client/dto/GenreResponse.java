package com.movieticket.bookingservice.infrastructure.client.dto;

public record GenreResponse(
        Long id,
        String name,
        String description
) {}
