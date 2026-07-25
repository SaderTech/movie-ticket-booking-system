package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.BookingResponse;

public interface GetBookingUseCase {
    BookingResponse findByBookingCode(String bookingCode, Long userId);
}
