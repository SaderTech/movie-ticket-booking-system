package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.application.command.ConfirmBookingCommand;

import java.util.Map;

public interface ConfirmBookingUseCase {
    BookingResponse execute(ConfirmBookingCommand command);
    BookingResponse handleVnPayReturn(Map<String, String> vnpayParams);
}
