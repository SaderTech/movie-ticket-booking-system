package com.movieticket.movieservice.application.usecase.genre;

import com.movieticket.movieservice.api.dto.request.CreateGenreRequest;
import com.movieticket.movieservice.api.dto.response.GenreResponse;
import com.movieticket.movieservice.api.exception.BusinessException;
import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import com.movieticket.movieservice.infrastructure.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateGenreUseCase {

    private final GenreRepository genreRepository;

    @Transactional
    @CacheEvict(value = "genres", allEntries = true)

    public GenreResponse execute(CreateGenreRequest request) {
        if (genreRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Genre name already exists: " + request.name());
        }

        Genre genre = new Genre(request.name(), request.description());
        Genre savedGenre = genreRepository.save(genre);

        return new GenreResponse(
                savedGenre.getId(),
                savedGenre.getName(),
                savedGenre.getDescription()
        );
    }
}