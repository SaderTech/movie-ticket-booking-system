package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.api.dto.response.MovieResponse;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetMoviesByStatusUseCase {

    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "movies", key = "'status:' + #p0.name()")

    public List<MovieResponse> execute(MovieStatus status) {
        return movieRepository.findByStatus(status)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }
}