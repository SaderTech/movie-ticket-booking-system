package com.movieticket.movieservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateActorRequest(

        @NotBlank(message = "Actor name must not be blank")
        String name,

        String avatarUrl,

        String biography,

        LocalDate birthDate
) {
}