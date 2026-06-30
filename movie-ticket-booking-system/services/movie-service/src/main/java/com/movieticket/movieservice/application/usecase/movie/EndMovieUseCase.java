package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.api.dto.response.MovieResponse;
import com.movieticket.movieservice.application.usecase.common.MovieReferenceResolver;
import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EndMovieUseCase {

    private final MovieRepository movieRepository;
    private final MovieReferenceResolver movieReferenceResolver;

    @Transactional
    @CacheEvict(value = "movies", allEntries = true)

    public MovieResponse execute(Long id) {
        Movie movie = movieReferenceResolver.findMovieById(id);
        movie.endMovie();

        Movie savedMovie = movieRepository.save(movie);
        return MovieResponse.from(savedMovie);
    }
}