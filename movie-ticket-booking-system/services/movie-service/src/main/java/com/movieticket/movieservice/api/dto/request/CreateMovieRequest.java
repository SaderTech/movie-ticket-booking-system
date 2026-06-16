package com.movieticket.movieservice.api.dto.request;

import com.movieticket.movieservice.domain.enums.AgeRating;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

public record CreateMovieRequest(
        @NotBlank(message = "Movie title is required")
        String title,

        String description,

        @NotNull(message = "Duration is required")
        @Positive(message = "Duration must be greater than 0")
        Integer durationMinutes,

        String trailerUrl,

        String posterUrl,

        LocalDate releaseDate,

        AgeRating ageRating,

        MovieStatus status,

        List<Long> genreIds,

        @Valid
        List<MovieActorRequest> actors,

        List<Long> directorIds
) {
}