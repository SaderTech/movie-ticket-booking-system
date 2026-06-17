package com.movieticket.cinemaservice.infrastructure.repository;

import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HallRepository extends JpaRepository<Hall, Long> {

    boolean existsByCinema_IdAndNameIgnoreCase(Long cinemaId, String name);

    Optional<Hall> findByCinema_IdAndNameIgnoreCase(Long cinemaId, String name);

    List<Hall> findByCinema_Id(Long cinemaId);

    List<Hall> findByStatus(HallStatus status);
}