package com.movieticket.movieservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreateActorRequest(
        @NotBlank(message = "Actor name is required")
        String name,

        String avatarUrl,

        String biography,

        LocalDate birthDate
) {
}