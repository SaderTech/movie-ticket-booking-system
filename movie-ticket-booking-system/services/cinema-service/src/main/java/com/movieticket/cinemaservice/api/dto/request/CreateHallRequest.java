package com.movieticket.cinemaservice.api.dto.request;

import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.HallType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateHallRequest(
        @NotNull(message = "Cinema id is required")
        Long cinemaId,

        @NotBlank(message = "Hall name is required")
        String name,

        @NotNull(message = "Capacity is required")
        @Positive(message = "Capacity must be greater than 0")
        Integer capacity,

        HallType hallType,

        HallStatus status
) {
}