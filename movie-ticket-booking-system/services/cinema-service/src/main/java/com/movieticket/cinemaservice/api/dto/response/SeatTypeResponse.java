package com.movieticket.cinemaservice.api.dto.response;

import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;

public record SeatTypeResponse(
        Long id,
        String code,
        String name,
        String description
) {

    public static SeatTypeResponse from(SeatType seatType) {
        return new SeatTypeResponse(
                seatType.getId(),
                seatType.getCode(),
                seatType.getName(),
                seatType.getDescription()
        );
    }
}