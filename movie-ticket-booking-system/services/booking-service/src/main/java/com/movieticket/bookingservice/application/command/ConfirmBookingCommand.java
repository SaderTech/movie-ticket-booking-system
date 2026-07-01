package com.movieticket.bookingservice.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ConfirmBookingCommand {
    private final Long userId;
    private final String holdToken;
    private final String paymentMethod;
    private final String returnUrl;
    private final String ipAddress;
}
