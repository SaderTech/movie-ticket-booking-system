package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.HoldSeatsResponse;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;

public interface HoldSeatsUseCase {
    HoldSeatsResponse execute(HoldSeatsCommand command);
}
