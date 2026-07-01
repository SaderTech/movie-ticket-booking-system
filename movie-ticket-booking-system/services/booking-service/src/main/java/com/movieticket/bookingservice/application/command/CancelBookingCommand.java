package com.movieticket.bookingservice.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CancelBookingCommand {
    private final Long userId;
    private final String bookingCode;
    private final String reason;
}
