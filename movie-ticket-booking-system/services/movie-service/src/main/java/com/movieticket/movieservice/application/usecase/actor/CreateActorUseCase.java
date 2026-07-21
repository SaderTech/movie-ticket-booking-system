package com.movieticket.movieservice.application.usecase.actor;

import com.movieticket.movieservice.application.dto.request.CreateActorRequest;
import com.movieticket.movieservice.application.dto.response.PersonResponse;
import com.movieticket.movieservice.domain.aggregate.actor.Actor;
import com.movieticket.movieservice.infrastructure.repository.ActorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateActorUseCase {

    private final ActorRepository actorRepository;

    @Transactional
    @CacheEvict(value = "actors", allEntries = true)

    public PersonResponse execute(CreateActorRequest request) {
        String normalizedName = normalizeName(request.name());

        Actor actor = new Actor(
                normalizedName,
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

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ");
    }
}
