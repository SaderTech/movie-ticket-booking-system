package com.movieticket.cinemaservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSeatTypeRequest(

        @NotBlank(message = "Seat type code must not be blank")
        @Size(max = 50, message = "Seat type code must not exceed 50 characters")
        String code,

        @NotBlank(message = "Seat type name must not be blank")
        @Size(max = 100, message = "Seat type name must not exceed 100 characters")
        String name,

        String description
) {
}
