package com.movieticket.movieservice.domain.aggregate.movie;

import com.movieticket.movieservice.domain.enums.AgeRating;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MovieTest {

    @Test
    void shouldCreateAndNormalizeValidComingSoonMovie() {
        Movie movie = new Movie(
                "  Demo   Movie  ",
                "  Description  ",
                120,
                "  https://example.com/trailer  ",
                "  https://example.com/poster  ",
                LocalDate.now().plusDays(1),
                AgeRating.C13,
                MovieStatus.COMING_SOON
        );

        assertThat(movie.getTitle()).isEqualTo("Demo Movie");
        assertThat(movie.getDescription()).isEqualTo("Description");
        assertThat(movie.getTrailerUrl()).isEqualTo("https://example.com/trailer");
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.COMING_SOON);
    }

    @Test
    void shouldDefaultToComingSoonWhenStatusIsNull() {
        Movie movie = validMovie(LocalDate.now().plusDays(1), null);

        assertThat(movie.getStatus()).isEqualTo(MovieStatus.COMING_SOON);
    }

    @Test
    void shouldRejectInvalidDuration() {
        assertThatThrownBy(() -> new Movie(
                "Demo Movie",
                "Description",
                0,
                "https://example.com/trailer",
                null,
                LocalDate.now().plusDays(1),
                AgeRating.C13,
                MovieStatus.COMING_SOON
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 1 and 600");
    }

    @Test
    void shouldRejectComingSoonMovieWithPastReleaseDate() {
        assertThatThrownBy(() -> validMovie(
                LocalDate.now().minusDays(1),
                MovieStatus.COMING_SOON
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("COMING_SOON");
    }

    @Test
    void shouldRejectNowShowingMovieWithFutureReleaseDate() {
        assertThatThrownBy(() -> validMovie(
                LocalDate.now().plusDays(1),
                MovieStatus.NOW_SHOWING
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NOW_SHOWING");
    }

    @Test
    void shouldMoveThroughValidLifecycle() {
        Movie movie = validMovie(LocalDate.now(), MovieStatus.COMING_SOON);

        movie.startShowing();
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.NOW_SHOWING);

        movie.endMovie();
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.ENDED);
    }

    @Test
    void shouldNotStartMovieBeforeReleaseDate() {
        Movie movie = validMovie(LocalDate.now().plusDays(1), MovieStatus.COMING_SOON);

        assertThatThrownBy(movie::startShowing)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("before its release date");
    }

    @Test
    void shouldNotEndMovieThatIsNotNowShowing() {
        Movie movie = validMovie(LocalDate.now().plusDays(1), MovieStatus.COMING_SOON);

        assertThatThrownBy(movie::endMovie)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("NOW_SHOWING");
    }

    @Test
    void shouldRejectBackwardStatusTransition() {
        Movie movie = validMovie(LocalDate.now().minusDays(1), MovieStatus.ENDED);

        assertThatThrownBy(() -> movie.updateBasicInfo(
                movie.getTitle(),
                movie.getDescription(),
                movie.getDurationMinutes(),
                movie.getTrailerUrl(),
                movie.getPosterUrl(),
                LocalDate.now(),
                movie.getAgeRating(),
                MovieStatus.NOW_SHOWING
        )).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid movie status transition");
    }

    private Movie validMovie(LocalDate releaseDate, MovieStatus status) {
        return new Movie(
                "Demo Movie",
                "Description",
                120,
                "https://example.com/trailer",
                "https://example.com/poster",
                releaseDate,
                AgeRating.C13,
                status
        );
    }
}
