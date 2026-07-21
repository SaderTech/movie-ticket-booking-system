package com.movieticket.cinemaservice.application.dto.request;

import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateCinemaRequest(
        @NotBlank(message = "Cinema name is required")
        @Size(max = 255, message = "Cinema name must not exceed 255 characters")
        String name,

        @NotBlank(message = "Cinema address is required")
        String address,

        @NotBlank(message = "Cinema city is required")
        @Size(max = 100, message = "Cinema city must not exceed 100 characters")
        String city,

        @Pattern(regexp = "^[0-9+()\\-\\s]{8,20}$", message = "Invalid phone number")
        String contactPhone,

        @DecimalMin(value = "-90.0", message = "Latitude must be at least -90.0")
        @DecimalMax(value = "90.0", message = "Latitude must be at most 90.0")
        BigDecimal latitude,

        @DecimalMin(value = "-180.0", message = "Longitude must be at least -180.0")
        @DecimalMax(value = "180.0", message = "Longitude must be at most 180.0")
        BigDecimal longitude,

        @NotNull(message = "Cinema status is required")
        CinemaStatus status
) {
}
