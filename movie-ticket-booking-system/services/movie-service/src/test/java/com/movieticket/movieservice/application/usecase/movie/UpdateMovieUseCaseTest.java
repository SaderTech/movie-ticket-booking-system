package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.application.dto.request.MovieActorRequest;
import com.movieticket.movieservice.application.dto.request.UpdateMovieRequest;
import com.movieticket.movieservice.application.exception.BusinessException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateMovieUseCaseTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private MovieReferenceResolver movieReferenceResolver;

    private UpdateMovieUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateMovieUseCase(movieRepository, movieReferenceResolver);
    }

    @Test
    void shouldUpdateMovieAndMoveStatusForward() {
        Movie movie = movie();
        UpdateMovieRequest request = request("  Updated   Title  ");
        when(movieReferenceResolver.findMovieById(1L)).thenReturn(movie);
        when(movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndIdNot(
                "Updated Title",
                request.releaseDate(),
                1L
        )).thenReturn(false);
        when(movieRepository.save(movie)).thenReturn(movie);

        var response = useCase.execute(1L, request);

        assertThat(response.title()).isEqualTo("Updated Title");
        assertThat(response.status()).isEqualTo(MovieStatus.NOW_SHOWING);
        verify(movieReferenceResolver).attachGenres(movie, request.genreIds());
        verify(movieReferenceResolver).attachActors(movie, request.actors());
        verify(movieReferenceResolver).attachDirectors(movie, request.directorIds());
    }

    @Test
    void shouldRejectDuplicateMovieBeforeMutatingAggregate() {
        Movie movie = movie();
        UpdateMovieRequest request = request("Duplicate Movie");
        when(movieReferenceResolver.findMovieById(1L)).thenReturn(movie);
        when(movieRepository.existsByTitleIgnoreCaseAndReleaseDateAndIdNot(
                "Duplicate Movie",
                request.releaseDate(),
                1L
        )).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(BusinessException.class);

        assertThat(movie.getTitle()).isEqualTo("Original Movie");
        verify(movieRepository, never()).save(any(Movie.class));
        verify(movieReferenceResolver, never()).attachGenres(any(Movie.class), eq(request.genreIds()));
    }

    private Movie movie() {
        return new Movie(
                "Original Movie",
                "Description",
                120,
                "https://example.com/trailer",
                null,
                LocalDate.now(),
                AgeRating.C13,
                MovieStatus.COMING_SOON
        );
    }

    private UpdateMovieRequest request(String title) {
        return new UpdateMovieRequest(
                title,
                "Updated description",
                130,
                "https://example.com/new-trailer",
                null,
                LocalDate.now(),
                AgeRating.C16,
                MovieStatus.NOW_SHOWING,
                List.of(1L),
                List.of(new MovieActorRequest(1L, "Lead")),
                List.of(1L)
        );
    }
}
