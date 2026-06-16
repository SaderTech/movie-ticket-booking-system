package com.movieticket.cinemaservice.api.dto.request;

import com.movieticket.cinemaservice.domain.enums.SeatStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateSeatRequest(
        @NotNull(message = "Hall id is required")
        Long hallId,

        @NotNull(message = "Seat type id is required")
        Long seatTypeId,

        @NotBlank(message = "Row name is required")
        String rowName,

        @NotNull(message = "Seat number is required")
        @Positive(message = "Seat number must be greater than 0")
        Integer seatNumber,

        SeatStatus status
) {
}