package com.movieticket.cinemaservice.infrastructure.repository;

import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HallRepository extends JpaRepository<Hall, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hall h where h.id = :id")
    Optional<Hall> findByIdForUpdate(@Param("id") Long id);

    boolean existsByCinema_IdAndNameIgnoreCase(Long cinemaId, String name);

    Optional<Hall> findByCinema_IdAndNameIgnoreCase(Long cinemaId, String name);

    @EntityGraph(attributePaths = "cinema")
    List<Hall> findByCinema_Id(Long cinemaId);

    @EntityGraph(attributePaths = {"cinema", "seats", "seats.seatType"})
    @Query("select distinct h from Hall h where h.id = :id")
    Optional<Hall> findDetailById(@Param("id") Long id);

    List<Hall> findByStatus(HallStatus status);
}
