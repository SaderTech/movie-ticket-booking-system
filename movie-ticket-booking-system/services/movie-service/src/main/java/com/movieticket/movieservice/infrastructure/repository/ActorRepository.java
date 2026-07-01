package com.movieticket.movieservice.infrastructure.repository;

import com.movieticket.movieservice.domain.aggregate.actor.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActorRepository extends JpaRepository<Actor, Long> {
    boolean existsByNameIgnoreCase(String name);
}