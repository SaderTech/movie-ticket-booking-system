package com.movieticket.movieservice.application.usecase.director;

import com.movieticket.movieservice.application.dto.response.PersonResponse;
import com.movieticket.movieservice.infrastructure.repository.DirectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllDirectorsUseCase {

    private final DirectorRepository directorRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "directors", key = "'all'")

    public List<PersonResponse> execute() {
        return directorRepository.findAll()
                .stream()
                .map(director -> new PersonResponse(
                        director.getId(),
                        director.getName(),
                        null,
                        director.getBiography(),
                        director.getBirthDate(),
                        null
                ))
                .toList();
    }
}