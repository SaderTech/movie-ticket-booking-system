package com.movieticket.bookingservice.infrastructure.client.dto;

public record SeatResponse(
        Long id,
        Long hallId,
        Long seatTypeId,
        String seatTypeCode,
        String seatTypeName,
        String rowName,
        Integer seatNumber,
        String status
) {}
