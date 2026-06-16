package com.movieticket.movieservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateDirectorRequest(
        @NotBlank(message = "Director name is required")
        String name,

        String biography,

        LocalDate birthDate
) {
}