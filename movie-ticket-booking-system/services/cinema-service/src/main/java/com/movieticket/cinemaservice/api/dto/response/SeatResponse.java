package com.movieticket.cinemaservice.api.dto.response;

import com.movieticket.cinemaservice.domain.aggregate.hall.Seat;
import com.movieticket.cinemaservice.domain.enums.SeatStatus;

public record SeatResponse(
        Long id,
        Long hallId,
        Long seatTypeId,
        String seatTypeCode,
        String seatTypeName,
        String rowName,
        Integer seatNumber,
        SeatStatus status
) {

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(
                seat.getId(),
                seat.getHall().getId(),
                seat.getSeatType().getId(),
                seat.getSeatType().getCode(),
                seat.getSeatType().getName(),
                seat.getRowName(),
                seat.getSeatNumber(),
                seat.getStatus()
        );
    }
}