package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.application.dto.request.CreateMovieRequest;
import com.movieticket.movieservice.application.dto.response.MovieResponse;
import com.movieticket.movieservice.application.exception.BusinessException;
import com.movieticket.movieservice.application.service.MovieReferenceResolver;
import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateMovieUseCase {

    private final MovieRepository movieRepository;
    private final MovieReferenceResolver movieReferenceResolver;

    @Transactional
    @CacheEvict(value = "movies", allEntries = true)

    public MovieResponse execute(CreateMovieRequest request) {
        String normalizedTitle = normalizeTitle(request.title());

        if (movieRepository.existsByTitleIgnoreCaseAndReleaseDate(
                normalizedTitle,
                request.releaseDate()
        )) {
            throw new BusinessException(
                    "Movie already exists with title and release date: "
                            + normalizedTitle + " - " + request.releaseDate()
            );
        }

        Movie movie = new Movie(
                normalizedTitle,
                request.description(),
                request.durationMinutes(),
                request.trailerUrl(),
                request.posterUrl(),
                request.releaseDate(),
                request.ageRating(),
                request.status()
        );

        movieReferenceResolver.attachGenres(movie, request.genreIds());
        movieReferenceResolver.attachActors(movie, request.actors());
        movieReferenceResolver.attachDirectors(movie, request.directorIds());

        Movie savedMovie = movieRepository.save(movie);

        return MovieResponse.from(savedMovie);
    }

    private String normalizeTitle(String title) {
        return title.trim().replaceAll("\\s+", " ");
    }
}
