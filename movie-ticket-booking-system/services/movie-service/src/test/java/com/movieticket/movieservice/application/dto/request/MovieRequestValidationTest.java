package com.movieticket.movieservice.application.dto.request;

import com.movieticket.movieservice.domain.enums.AgeRating;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MovieRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void shouldAcceptValidCreateRequest() {
        CreateMovieRequest request = validRequest();

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void shouldRejectMissingRequiredReferences() {
        CreateMovieRequest request = new CreateMovieRequest(
                "Demo Movie",
                "Description",
                120,
                "https://example.com/trailer",
                null,
                LocalDate.now().plusDays(1),
                AgeRating.C13,
                MovieStatus.COMING_SOON,
                List.of(),
                List.of(),
                List.of()
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("genreIds", "actors", "directorIds");
    }

    @Test
    void shouldRejectInvalidUrlsAndDuration() {
        CreateMovieRequest request = new CreateMovieRequest(
                "Demo Movie",
                "Description",
                601,
                "not-a-url",
                "also-not-a-url",
                LocalDate.now().plusDays(1),
                AgeRating.C13,
                MovieStatus.COMING_SOON,
                List.of(1L),
                List.of(new MovieActorRequest(1L, "Lead")),
                List.of(1L)
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("durationMinutes", "trailerUrl", "posterUrl");
    }

    @Test
    void shouldValidateNestedActorRole() {
        CreateMovieRequest request = new CreateMovieRequest(
                "Demo Movie",
                "Description",
                120,
                "https://example.com/trailer",
                null,
                LocalDate.now().plusDays(1),
                AgeRating.C13,
                MovieStatus.COMING_SOON,
                List.of(1L),
                List.of(new MovieActorRequest(1L, " ")),
                List.of(1L)
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("actors[0].roleName");
    }

    @Test
    void shouldRequireStatusWhenUpdatingMovie() {
        UpdateMovieRequest request = new UpdateMovieRequest(
                "Demo Movie",
                "Description",
                120,
                "https://example.com/trailer",
                null,
                LocalDate.now(),
                AgeRating.C13,
                null,
                List.of(1L),
                List.of(new MovieActorRequest(1L, "Lead")),
                List.of(1L)
        );

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("status");
    }

    private CreateMovieRequest validRequest() {
        return new CreateMovieRequest(
                "Demo Movie",
                "Description",
                120,
                "https://example.com/trailer",
                "https://example.com/poster",
                LocalDate.now().plusDays(1),
                AgeRating.C13,
                MovieStatus.COMING_SOON,
                List.of(1L),
                List.of(new MovieActorRequest(1L, "Lead")),
                List.of(1L)
        );
    }
}
