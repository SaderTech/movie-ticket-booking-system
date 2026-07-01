package com.movieticket.cinemaservice.api.dto.response;

import com.movieticket.cinemaservice.domain.aggregate.hall.HallMaintenance;
import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;

import java.time.LocalDateTime;

public record HallMaintenanceResponse(
        Long id,
        Long hallId,
        String hallName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String reason,
        MaintenanceStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static HallMaintenanceResponse from(HallMaintenance maintenance) {
        return new HallMaintenanceResponse(
                maintenance.getId(),
                maintenance.getHall().getId(),
                maintenance.getHall().getName(),
                maintenance.getStartTime(),
                maintenance.getEndTime(),
                maintenance.getReason(),
                maintenance.getStatus(),
                maintenance.getCreatedAt(),
                maintenance.getUpdatedAt()
        );
    }
}