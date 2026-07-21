package com.movieticket.movieservice.application.usecase.director;

import com.movieticket.movieservice.application.dto.request.UpdateDirectorRequest;
import com.movieticket.movieservice.application.dto.response.PersonResponse;
import com.movieticket.movieservice.application.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.director.Director;
import com.movieticket.movieservice.infrastructure.repository.DirectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateDirectorUseCase {

    private final DirectorRepository directorRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "directors", allEntries = true),
            @CacheEvict(value = "movies", allEntries = true)
    })
    public PersonResponse execute(Long id, UpdateDirectorRequest request) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id: " + id));
        String normalizedName = normalizeName(request.name());

        director.update(
                normalizedName,
                request.biography(),
                request.birthDate()
        );

        Director savedDirector = directorRepository.save(director);

        return new PersonResponse(
                savedDirector.getId(),
                savedDirector.getName(),
                null,
                savedDirector.getBiography(),
                savedDirector.getBirthDate(),
                null
        );
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}
