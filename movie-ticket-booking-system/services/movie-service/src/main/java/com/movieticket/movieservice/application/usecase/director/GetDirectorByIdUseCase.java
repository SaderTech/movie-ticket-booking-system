package com.movieticket.movieservice.application.usecase.director;

import com.movieticket.movieservice.api.dto.response.PersonResponse;
import com.movieticket.movieservice.api.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.director.Director;
import com.movieticket.movieservice.infrastructure.repository.DirectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetDirectorByIdUseCase {

    private final DirectorRepository directorRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "directors", key = "#id")

    public PersonResponse execute(Long id) {
        Director director = directorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Director not found with id: " + id));

        return new PersonResponse(
                director.getId(),
                director.getName(),
                null,
                director.getBiography(),
                director.getBirthDate(),
                null
        );
    }
}