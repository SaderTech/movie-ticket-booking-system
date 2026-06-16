package com.movieticket.cinemaservice.api.dto.request;

import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateMaintenanceStatusRequest(
        @NotNull(message = "Maintenance status is required")
        MaintenanceStatus status
) {
}