package com.movieticket.movieservice.application.usecase.actor;

import com.movieticket.movieservice.api.dto.request.UpdateActorRequest;
import com.movieticket.movieservice.api.dto.response.PersonResponse;
import com.movieticket.movieservice.api.exception.BusinessException;
import com.movieticket.movieservice.api.exception.ResourceNotFoundException;
import com.movieticket.movieservice.domain.aggregate.actor.Actor;
import com.movieticket.movieservice.infrastructure.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateActorUseCase {

    private final ActorRepository actorRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "actors", allEntries = true),
            @CacheEvict(value = "movies", allEntries = true)
    })
    public PersonResponse execute(Long id, UpdateActorRequest request) {
        Actor actor = actorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found with id: " + id));

        if (!actor.getName().equalsIgnoreCase(request.name())
                && actorRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Actor name already exists: " + request.name());
        }

        actor.update(
                request.name(),
                request.avatarUrl(),
                request.biography(),
                request.birthDate()
        );

        Actor savedActor = actorRepository.save(actor);

        return new PersonResponse(
                savedActor.getId(),
                savedActor.getName(),
                savedActor.getAvatarUrl(),
                savedActor.getBiography(),
                savedActor.getBirthDate(),
                null
        );
    }
}