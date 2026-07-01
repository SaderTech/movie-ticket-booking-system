package com.movieticket.movieservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateDirectorRequest(

        @NotBlank(message = "Director name must not be blank")
        String name,

        String biography,

        LocalDate birthDate
) {
}