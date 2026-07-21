package com.movieticket.movieservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateGenreRequest(

        @NotBlank(message = "Genre name must not be blank")
        @Size(max = 100, message = "Genre name must not exceed 100 characters")
        String name,

        @Size(max = 2000, message = "Genre description must not exceed 2000 characters")
        String description
) {
}
