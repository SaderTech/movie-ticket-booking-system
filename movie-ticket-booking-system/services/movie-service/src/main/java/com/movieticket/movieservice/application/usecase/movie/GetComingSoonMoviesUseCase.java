package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.application.dto.response.MovieResponse;
import com.movieticket.movieservice.domain.enums.MovieStatus;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetComingSoonMoviesUseCase {

    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "movies", key = "'coming-soon'", unless = "#result == null || #result.isEmpty()")

    public List<MovieResponse> execute() {
        return movieRepository.findByStatus(MovieStatus.COMING_SOON)
                .stream()
                .map(MovieResponse::from)
                .toList();
    }
}
