package com.movieticket.showtimeservice.application.dto.request;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class ReleaseSeatRequest {


    @NotNull(message = "Seat quantity is required")
    @Min(value = 1, message = "Seat quantity must be greater than 0")
    private Integer quantity;

}