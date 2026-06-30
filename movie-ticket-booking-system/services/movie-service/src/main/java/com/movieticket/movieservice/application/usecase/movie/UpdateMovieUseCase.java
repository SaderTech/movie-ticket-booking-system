package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.api.dto.request.UpdateMovieRequest;
import com.movieticket.movieservice.api.dto.response.MovieResponse;
import com.movieticket.movieservice.api.exception.BusinessException;
import com.movieticket.movieservice.application.usecase.common.MovieReferenceResolver;
import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateMovieUseCase {

    private final MovieRepository movieRepository;
    private final MovieReferenceResolver movieReferenceResolver;

    @Transactional
    @CacheEvict(value = "movies", allEntries = true)

    public MovieResponse execute(Long id, UpdateMovieRequest request) {
        Movie movie = movieReferenceResolver.findMovieById(id);

        if (request.title() != null
                && !movie.getTitle().equalsIgnoreCase(request.title())
                && movieRepository.existsByTitleIgnoreCase(request.title())) {
            throw new BusinessException("Movie title already exists: " + request.title());
        }

        movie.updateBasicInfo(
                request.title(),
                request.description(),
                request.durationMinutes(),
                request.trailerUrl(),
                request.posterUrl(),
                request.releaseDate(),
                request.ageRating(),
                request.status()
        );

        movie.clearGenres();
        movie.clearActors();
        movie.clearDirectors();

        movieReferenceResolver.attachGenres(movie, request.genreIds());
        movieReferenceResolver.attachActors(movie, request.actors());
        movieReferenceResolver.attachDirectors(movie, request.directorIds());

        Movie savedMovie = movieRepository.save(movie);

        return MovieResponse.from(savedMovie);
    }
}