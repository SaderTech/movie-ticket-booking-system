package com.movieticket.movieservice.application.usecase.director;

import com.movieticket.movieservice.application.dto.request.CreateDirectorRequest;
import com.movieticket.movieservice.application.dto.response.PersonResponse;
import com.movieticket.movieservice.domain.aggregate.director.Director;
import com.movieticket.movieservice.infrastructure.repository.DirectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateDirectorUseCase {

    private final DirectorRepository directorRepository;

    @Transactional
    @CacheEvict(value = "directors", allEntries = true)

    public PersonResponse execute(CreateDirectorRequest request) {
        String normalizedName = normalizeName(request.name());

        Director director = new Director(
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
