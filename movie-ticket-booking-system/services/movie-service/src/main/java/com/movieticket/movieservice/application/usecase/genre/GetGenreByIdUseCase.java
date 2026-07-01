package com.movieticket.movieservice.application.usecase.genre;

import com.movieticket.movieservice.api.dto.response.GenreResponse;
import com.movieticket.movieservice.api.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import com.movieticket.movieservice.infrastructure.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetGenreByIdUseCase {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "genres", key = "#p0")

    public GenreResponse execute(Long id) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));

        return new GenreResponse(
                genre.getId(),
                genre.getName(),
                genre.getDescription()
        );
    }
}