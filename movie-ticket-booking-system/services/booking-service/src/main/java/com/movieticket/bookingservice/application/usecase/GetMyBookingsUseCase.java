package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.BookingResponse;

import java.util.List;

public interface GetMyBookingsUseCase {
    List<BookingResponse> findByUserId(Long userId);
}
