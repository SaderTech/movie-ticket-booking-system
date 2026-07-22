package com.movieticket.bookingservice.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ShowtimeResponse(
        Long id,
        Long movieId,
        Long cinemaId,
        Long roomId,
        LocalDate showDate,
        LocalTime startTime,
        LocalTime endTime,
        BigDecimal price,
        Integer availableSeats,
        String status
) {}
