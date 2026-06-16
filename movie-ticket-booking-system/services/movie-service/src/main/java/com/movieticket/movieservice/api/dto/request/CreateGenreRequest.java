package com.movieticket.movieservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateGenreRequest(
        @NotBlank(message = "Genre name is required")
        String name,

        String description
) {
}