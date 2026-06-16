package com.movieticket.movieservice.infrastructure.repository;

import com.movieticket.movieservice.domain.aggregate.director.Director;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectorRepository extends JpaRepository<Director, Long> {
}