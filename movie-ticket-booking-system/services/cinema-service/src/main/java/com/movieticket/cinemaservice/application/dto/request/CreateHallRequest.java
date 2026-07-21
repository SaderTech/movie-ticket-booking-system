package com.movieticket.cinemaservice.application.dto.request;

import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.HallType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateHallRequest(
        @NotNull(message = "Cinema id is required")
        Long cinemaId,

        @NotBlank(message = "Hall name is required")
        @Size(max = 255, message = "Hall name must not exceed 255 characters")
        String name,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be greater than 0")
        Integer capacity,

        @NotNull(message = "Hall type is required")
        HallType hallType,

        @NotNull(message = "Hall status is required")
        HallStatus status
) {
}
