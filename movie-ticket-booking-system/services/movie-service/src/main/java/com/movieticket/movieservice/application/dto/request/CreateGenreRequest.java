package com.movieticket.movieservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateGenreRequest(
        @NotBlank(message = "Genre name is required")
        @Size(max = 100, message = "Genre name must not exceed 100 characters")
        String name,

        @Size(max = 2000, message = "Genre description must not exceed 2000 characters")
        String description
) {
}
