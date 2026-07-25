package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.SeatAvailabilityResponse;

public interface GetSeatAvailabilityUseCase {
    SeatAvailabilityResponse findByShowtimeId(Long showtimeId);
}
