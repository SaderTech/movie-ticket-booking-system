package com.movieticket.movieservice.application.usecase.genre;

import com.movieticket.movieservice.api.dto.request.UpdateGenreRequest;
import com.movieticket.movieservice.api.dto.response.GenreResponse;
import com.movieticket.movieservice.api.exception.BusinessException;
import com.movieticket.movieservice.api.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import com.movieticket.movieservice.infrastructure.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateGenreUseCase {

    private final GenreRepository genreRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "genres", allEntries = true),
            @CacheEvict(value = "movies", allEntries = true)
    })
    public GenreResponse execute(Long id, UpdateGenreRequest request) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Genre not found with id: " + id));

        if (!genre.getName().equalsIgnoreCase(request.name())
                && genreRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Genre name already exists: " + request.name());
        }

        genre.update(request.name(), request.description());

        Genre savedGenre = genreRepository.save(genre);

        return new GenreResponse(
                savedGenre.getId(),
                savedGenre.getName(),
                savedGenre.getDescription()
        );
    }
}