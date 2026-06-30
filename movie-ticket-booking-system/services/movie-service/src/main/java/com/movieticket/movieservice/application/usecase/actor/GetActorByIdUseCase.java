package com.movieticket.movieservice.application.usecase.actor;

import com.movieticket.movieservice.api.dto.response.PersonResponse;
import com.movieticket.movieservice.api.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.actor.Actor;
import com.movieticket.movieservice.infrastructure.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetActorByIdUseCase {

    private final ActorRepository actorRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "actors", key = "#p0")

    public PersonResponse execute(Long id) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + id));

        return new PersonResponse(
                actor.getId(),
                actor.getName(),
                actor.getAvatarUrl(),
                actor.getBiography(),
                actor.getBirthDate(),
                null
        );
    }
}