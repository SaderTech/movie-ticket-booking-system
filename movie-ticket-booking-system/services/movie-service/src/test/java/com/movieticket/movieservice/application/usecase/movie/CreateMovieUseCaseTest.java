package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.application.dto.request.CreateMovieRequest;
import com.movieticket.movieservice.application.dto.request.MovieActorRequest;
import com.movieticket.movieservice.application.dto.response.MovieResponse;
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
class CreateMovieUseCaseTest {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private MovieReferenceResolver movieReferenceResolver;

    private CreateMovieUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateMovieUseCase(movieRepository, movieReferenceResolver);
    }

    @Test
    void shouldCreateMovieAndNormalizeTitle() {
        CreateMovieRequest request = validRequest("  Demo   Movie  ");
        when(movieRepository.existsByTitleIgnoreCaseAndReleaseDate(
                "Demo Movie",
                request.releaseDate()
        )).thenReturn(false);
        when(movieRepository.save(any(Movie.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MovieResponse response = useCase.execute(request);

        assertThat(response.title()).isEqualTo("Demo Movie");
        verify(movieReferenceResolver).attachGenres(any(Movie.class), eq(request.genreIds()));
        verify(movieReferenceResolver).attachActors(any(Movie.class), eq(request.actors()));
        verify(movieReferenceResolver).attachDirectors(any(Movie.class), eq(request.directorIds()));
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void shouldRejectDuplicateTitleAndReleaseDate() {
        CreateMovieRequest request = validRequest("Demo Movie");
        when(movieRepository.existsByTitleIgnoreCaseAndReleaseDate(
                "Demo Movie",
                request.releaseDate()
        )).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("title and release date");

        verify(movieRepository, never()).save(any(Movie.class));
    }

    private CreateMovieRequest validRequest(String title) {
        return new CreateMovieRequest(
                title,
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
