package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.application.command.CancelBookingCommand;

public interface CancelBookingUseCase {
    BookingResponse execute(CancelBookingCommand command);
}
