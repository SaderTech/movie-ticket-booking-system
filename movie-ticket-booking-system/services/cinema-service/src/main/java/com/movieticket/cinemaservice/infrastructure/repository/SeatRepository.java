package com.movieticket.cinemaservice.infrastructure.repository;

import com.movieticket.cinemaservice.domain.aggregate.hall.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    long countByHall_Id(Long hallId);

    boolean existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(Long hallId, String rowName, Integer seatNumber);

    Optional<Seat> findByHall_IdAndRowNameIgnoreCaseAndSeatNumber(Long hallId, String rowName, Integer seatNumber);

    List<Seat> findByHall_IdOrderByRowNameAscSeatNumberAsc(Long hallId);
}
