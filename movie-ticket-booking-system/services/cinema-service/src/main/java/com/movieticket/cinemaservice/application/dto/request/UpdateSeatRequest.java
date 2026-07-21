package com.movieticket.cinemaservice.application.dto.request;

import com.movieticket.cinemaservice.domain.enums.SeatStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateSeatRequest(
        @NotNull(message = "Seat type id is required")
        Long seatTypeId,

        @NotBlank(message = "Row name is required")
        @Size(max = 10, message = "Row name must not exceed 10 characters")
        String rowName,

        @NotNull(message = "Seat number is required")
        @Positive(message = "Seat number must be greater than 0")
        Integer seatNumber,

        @NotNull(message = "Seat status is required")
        SeatStatus status
) {
}
