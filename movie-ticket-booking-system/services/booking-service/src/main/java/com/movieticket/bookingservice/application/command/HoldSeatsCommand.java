package com.movieticket.bookingservice.application.command;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class HoldSeatsCommand {
    private final Long userId;
    private final Long showtimeId;
    private final List<String> seatCodes;
}
