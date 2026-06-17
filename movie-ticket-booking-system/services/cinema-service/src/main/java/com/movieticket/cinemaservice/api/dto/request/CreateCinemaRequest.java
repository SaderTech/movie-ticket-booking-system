package com.movieticket.cinemaservice.api.dto.request;

import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateCinemaRequest(
        @NotBlank(message = "Cinema name is required")
        String name,

        String address,

        String city,

        String contactPhone,

        BigDecimal latitude,

        BigDecimal longitude,

        CinemaStatus status
) {
}