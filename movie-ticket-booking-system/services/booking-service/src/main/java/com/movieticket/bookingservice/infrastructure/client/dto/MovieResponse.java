package com.movieticket.bookingservice.infrastructure.client.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record MovieResponse(
        Long id,
        String title,
        String description,
        Integer durationMinutes,
        String trailerUrl,
        String posterUrl,
        LocalDate releaseDate,
        String ageRating,
        String status,
        List<GenreResponse> genres,
        List<PersonResponse> actors,
        List<PersonResponse> directors,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
