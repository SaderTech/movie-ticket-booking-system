package com.movieticket.movieservice.application.usecase.actor;

import com.movieticket.movieservice.api.dto.response.PersonResponse;
import com.movieticket.movieservice.infrastructure.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllActorsUseCase {

    private final ActorRepository actorRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "actors", key = "'all'")

    public List<PersonResponse> execute() {
        return actorRepository.findAll()
                .stream()
                .map(actor -> new PersonResponse(
                        actor.getId(),
                        actor.getName(),
                        actor.getAvatarUrl(),
                        actor.getBiography(),
                        actor.getBirthDate(),
                        null
                ))
                .toList();
    }
}