package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.application.service.MovieReferenceResolver;
import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.domain.enums.AgeRating;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieLifecycleUseCaseTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieReferenceResolver movieReferenceResolver;

    private StartMovieUseCase startMovieUseCase;
    private EndMovieUseCase endMovieUseCase;

    @BeforeEach
    void setUp() {
        startMovieUseCase = new StartMovieUseCase(movieRepository, movieReferenceResolver);
        endMovieUseCase = new EndMovieUseCase(movieRepository, movieReferenceResolver);
    }

    @Test
    void shouldStartReleasedComingSoonMovie() {
        Movie movie = movie(MovieStatus.COMING_SOON, LocalDate.now());
        when(movieReferenceResolver.findMovieById(1L)).thenReturn(movie);
        when(movieRepository.save(movie)).thenReturn(movie);

        var response = startMovieUseCase.execute(1L);

        assertThat(response.status()).isEqualTo(MovieStatus.NOW_SHOWING);
        verify(movieRepository).save(movie);
    }

    @Test
    void shouldEndNowShowingMovie() {
        Movie movie = movie(MovieStatus.NOW_SHOWING, LocalDate.now().minusDays(1));
        when(movieReferenceResolver.findMovieById(1L)).thenReturn(movie);
        when(movieRepository.save(movie)).thenReturn(movie);

        var response = endMovieUseCase.execute(1L);

        assertThat(response.status()).isEqualTo(MovieStatus.ENDED);
        verify(movieRepository).save(movie);
    }

    @Test
    void shouldNotPersistInvalidLifecycleTransition() {
        Movie movie = movie(MovieStatus.COMING_SOON, LocalDate.now().plusDays(1));
        when(movieReferenceResolver.findMovieById(1L)).thenReturn(movie);

        assertThatThrownBy(() -> startMovieUseCase.execute(1L))
                .isInstanceOf(IllegalStateException.class);

        verify(movieRepository, never()).save(movie);
    }

    private Movie movie(MovieStatus status, LocalDate releaseDate) {
        return new Movie(
                "Demo Movie",
                "Description",
                120,
                "https://example.com/trailer",
                null,
                releaseDate,
                AgeRating.C13,
                status
        );
    }
}
