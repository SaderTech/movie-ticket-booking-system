package com.movieticket.movieservice.infrastructure.repository;

import com.movieticket.movieservice.domain.aggregate.genre.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, Long> {

    boolean existsByNameIgnoreCase(String name);
}