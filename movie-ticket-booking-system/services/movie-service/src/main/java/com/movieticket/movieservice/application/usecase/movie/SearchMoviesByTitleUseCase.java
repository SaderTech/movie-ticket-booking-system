package com.movieticket.movieservice.application.usecase.movie;

import com.movieticket.movieservice.application.dto.response.MovieResponse;
import com.movieticket.movieservice.infrastructure.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchMoviesByTitleUseCase {

    private final MovieRepository movieRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "movies", key = "'search:' + (#p0 == null || #p0.isBlank() ? 'all' : #p0.trim().toLowerCase())")

    public List<MovieResponse> execute(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return movieRepository.findAll()
                    .stream()
                    .map(MovieResponse::from)
                    .toList();
        }

        return movieRepository.findByTitleContainingIgnoreCase(keyword.trim())
                .stream()
                .map(MovieResponse::from)
                .toList();
    }
}