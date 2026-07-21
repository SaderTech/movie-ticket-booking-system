package com.movieticket.movieservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;

public record CreateActorRequest(
        @NotBlank(message = "Actor name is required")
        @Size(max = 255, message = "Actor name must not exceed 255 characters")
        String name,

        @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
        @URL(message = "Avatar URL must be valid")
        String avatarUrl,

        @Size(max = 5000, message = "Biography must not exceed 5000 characters")
        String biography,

        @Past(message = "Actor birth date must be in the past")
        LocalDate birthDate
) {
}
