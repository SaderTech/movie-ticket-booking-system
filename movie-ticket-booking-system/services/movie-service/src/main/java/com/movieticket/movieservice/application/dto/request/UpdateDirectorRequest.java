package com.movieticket.movieservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateDirectorRequest(

        @NotBlank(message = "Director name must not be blank")
        @Size(max = 255, message = "Director name must not exceed 255 characters")
        String name,

        @Size(max = 5000, message = "Biography must not exceed 5000 characters")
        String biography,

        @Past(message = "Director birth date must be in the past")
        LocalDate birthDate
) {
}
