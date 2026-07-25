package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.dto.PagedResponse;

public interface GetMyBookingsUseCase {
    PagedResponse<BookingResponse> findByUserId(Long userId, int page, int size);
}
