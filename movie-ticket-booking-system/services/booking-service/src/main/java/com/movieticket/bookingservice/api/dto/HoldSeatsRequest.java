package com.movieticket.bookingservice.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoldSeatsRequest {
    @NotNull(message = "showtimeId is required")
    private Long showtimeId;

    @NotEmpty(message = "At least one seat code is required")
    @Size(max = 8, message = "Maximum 8 seats per hold request")
    private List<@NotEmpty String> seatCodes;
}
