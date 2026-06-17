package com.movieticket.cinemaservice.api.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateHallMaintenanceRequest(
        @NotNull(message = "Hall id is required")
        Long hallId,

        @NotNull(message = "Start time is required")
        LocalDateTime startTime,

        @NotNull(message = "End time is required")
        LocalDateTime endTime,

        String reason
) {
}