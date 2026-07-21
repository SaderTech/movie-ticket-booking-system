package com.movieticket.movieservice.application.usecase.genre;

import com.movieticket.movieservice.application.dto.response.GenreResponse;
import com.movieticket.movieservice.infrastructure.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllGenresUseCase {

    private final GenreRepository genreRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "genres", key = "'all'")

    public List<GenreResponse> execute() {
        return genreRepository.findAll()
                .stream()
                .map(genre -> new GenreResponse(
                        genre.getId(),
                        genre.getName(),
                        genre.getDescription()
                ))
                .toList();
    }
}