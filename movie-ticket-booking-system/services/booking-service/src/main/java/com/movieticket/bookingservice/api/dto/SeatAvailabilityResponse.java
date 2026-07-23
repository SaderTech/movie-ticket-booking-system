package com.movieticket.bookingservice.api.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SeatAvailabilityResponse(
        Long showtimeId,
        List<String> heldSeatCodes,
        List<String> bookedSeatCodes
) {}
