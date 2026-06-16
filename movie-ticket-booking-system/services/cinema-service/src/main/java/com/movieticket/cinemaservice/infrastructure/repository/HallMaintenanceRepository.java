package com.movieticket.cinemaservice.infrastructure.repository;

import com.movieticket.cinemaservice.domain.aggregate.hall.HallMaintenance;
import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HallMaintenanceRepository extends JpaRepository<HallMaintenance, Long> {

    List<HallMaintenance> findByHall_IdOrderByStartTimeDesc(Long hallId);

    @Query("""
            SELECT hm FROM HallMaintenance hm
            WHERE hm.hall.id = :hallId
              AND hm.status <> :cancelledStatus
              AND hm.startTime < :endTime
              AND hm.endTime > :startTime
            """)
    List<HallMaintenance> findOverlappingMaintenances(
            @Param("hallId") Long hallId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("cancelledStatus") MaintenanceStatus cancelledStatus
    );
}