package com.movieticket.movieservice.application.service;

import com.movieticket.movieservice.application.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.domain.enums.AgeRating;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import com.movieticket.movieservice.infrastructure.repository.ActorRepository;
import com.movieticket.movieservice.infrastructure.repository.DirectorRepository;
import com.movieticket.movieservice.infrastructure.repository.GenreRepository;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovieReferenceResolverTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private ActorRepository actorRepository;
    @Mock
    private DirectorRepository directorRepository;

    private MovieReferenceResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new MovieReferenceResolver(
                movieRepository,
                genreRepository,
                actorRepository,
                directorRepository
        );
    }

    @Test
    void shouldAttachExistingGenre() {
        Movie movie = validMovie();
        Genre genre = new Genre("Science Fiction", "Description");
        ReflectionTestUtils.setField(genre, "id", 1L);
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));

        resolver.attachGenres(movie, List.of(1L));

        assertThat(movie.getMovieGenres()).hasSize(1);
        assertThat(movie.getMovieGenres().getFirst().getGenre()).isSameAs(genre);
    }

    @Test
    void shouldRejectDuplicateReferenceIdsBeforeQueryingRepository() {
        Movie movie = validMovie();

        assertThatThrownBy(() -> resolver.attachGenres(movie, List.of(1L, 1L)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate ids");

        verify(genreRepository, never()).findById(1L);
    }

    @Test
    void shouldRejectMissingReference() {
        Movie movie = validMovie();
        when(genreRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> resolver.attachGenres(movie, List.of(99L)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    private Movie validMovie() {
        return new Movie(
                "Demo Movie",
                "Description",
                120,
                "https://example.com/trailer",
                null,
                LocalDate.now().plusDays(1),
                AgeRating.C13,
                MovieStatus.COMING_SOON
        );
    }
}
