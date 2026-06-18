package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.application.command.ConfirmBookingCommand;

public interface ConfirmBookingUseCase {
    BookingResponse execute(ConfirmBookingCommand command);
}
