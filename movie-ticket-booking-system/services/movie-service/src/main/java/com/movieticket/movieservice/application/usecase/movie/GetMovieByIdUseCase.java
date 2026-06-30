package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.api.dto.response.MovieResponse;
import com.movieticket.movieservice.application.usecase.common.MovieReferenceResolver;
import com.movieticket.movieservice.domain.aggregate.movie.Movie;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMovieByIdUseCase {

    private final MovieReferenceResolver movieReferenceResolver;

    @Transactional(readOnly = true)
    @Cacheable(value = "movies", key = "#id")

    public MovieResponse execute(Long id) {
        Movie movie = movieReferenceResolver.findMovieById(id);
        return MovieResponse.from(movie);
    }
}