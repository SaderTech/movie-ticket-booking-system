package com.movieticket.movieservice.api.dto.response;

public record GenreResponse(
        Long id,
        String name,
        String description
) {
}