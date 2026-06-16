package com.movieticket.cinemaservice.infrastructure.repository;

import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeatTypeRepository extends JpaRepository<SeatType, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<SeatType> findByCodeIgnoreCase(String code);
}