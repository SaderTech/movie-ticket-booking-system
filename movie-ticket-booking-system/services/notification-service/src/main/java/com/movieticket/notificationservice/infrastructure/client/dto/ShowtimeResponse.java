package com.movieticket.notificationservice.infrastructure.client.dto;

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
        String status
) {
}
