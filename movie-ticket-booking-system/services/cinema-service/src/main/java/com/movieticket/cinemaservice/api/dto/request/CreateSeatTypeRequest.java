package com.movieticket.cinemaservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSeatTypeRequest(
        @NotBlank(message = "Seat type code is required")
        String code,

        @NotBlank(message = "Seat type name is required")
        String name,

        String description
) {
}