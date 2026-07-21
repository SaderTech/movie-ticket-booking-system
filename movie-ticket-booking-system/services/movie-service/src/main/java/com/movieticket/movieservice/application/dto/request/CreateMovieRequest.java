package com.movieticket.movieservice.application.dto.request;

import com.movieticket.movieservice.domain.enums.AgeRating;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.util.List;

public record CreateMovieRequest(
        @NotBlank(message = "Movie title is required")
        @Size(max = 255, message = "Movie title must not exceed 255 characters")
        String title,

        @Size(max = 5000, message = "Description must not exceed 5000 characters")
        String description,

        @NotNull(message = "Duration is required")
        @Positive(message = "Duration must be greater than 0")
        @Max(value = 600, message = "Duration must not exceed 600 minutes")
        Integer durationMinutes,

        @NotBlank(message = "Trailer URL is required")
        @Size(max = 500, message = "Trailer URL must not exceed 500 characters")
        @URL(message = "Trailer URL must be valid")
        String trailerUrl,

        @Size(max = 500, message = "Poster URL must not exceed 500 characters")
        @URL(message = "Poster URL must be valid")
        String posterUrl,

        @NotNull(message = "Release date is required")
        LocalDate releaseDate,

        @NotNull(message = "Age rating is required")
        AgeRating ageRating,

        MovieStatus status,

        @NotEmpty(message = "Movie must have at least one genre")
        List<@NotNull(message = "Genre id must not be null") Long> genreIds,

        @NotEmpty(message = "Movie must have at least one actor")
        @Valid
        List<MovieActorRequest> actors,

        @NotEmpty(message = "Movie must have at least one director")
        List<@NotNull(message = "Director id must not be null") Long> directorIds
) {
}
