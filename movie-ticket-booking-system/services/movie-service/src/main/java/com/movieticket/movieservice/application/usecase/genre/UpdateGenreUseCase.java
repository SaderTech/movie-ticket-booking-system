package com.movieticket.movieservice.application.usecase.genre;

import com.movieticket.movieservice.application.dto.request.UpdateGenreRequest;
import com.movieticket.movieservice.application.dto.response.GenreResponse;
import com.movieticket.movieservice.application.exception.BusinessException;
import com.movieticket.movieservice.application.exception.ResourceNotFoundException;
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
        String normalizedName = normalizeName(request.name());

        if (!genre.getName().equalsIgnoreCase(normalizedName)
                && genreRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException("Genre name already exists: " + normalizedName);
        }

        genre.update(normalizedName, request.description());

        Genre savedGenre = genreRepository.save(genre);

        return new GenreResponse(
                savedGenre.getId(),
                savedGenre.getName(),
                savedGenre.getDescription()
        );
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}
