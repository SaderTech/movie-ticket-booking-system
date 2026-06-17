package com.movieticket.cinemaservice.infrastructure.repository;

import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Cinema> findByStatus(CinemaStatus status);
}