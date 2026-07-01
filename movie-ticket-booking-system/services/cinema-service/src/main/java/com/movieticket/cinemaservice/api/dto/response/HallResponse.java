package com.movieticket.cinemaservice.api.dto.response;

import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.HallType;

import java.time.LocalDateTime;
import java.util.List;

public record HallResponse(
        Long id,
        Long cinemaId,
        String cinemaName,
        String name,
        Integer capacity,
        HallType hallType,
        HallStatus status,
        List<SeatResponse> seats,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static HallResponse from(Hall hall) {
        List<SeatResponse> seats = hall.getSeats()
                .stream()
                .map(SeatResponse::from)
                .toList();

        return new HallResponse(
                hall.getId(),
                hall.getCinema().getId(),
                hall.getCinema().getName(),
                hall.getName(),
                hall.getCapacity(),
                hall.getHallType(),
                hall.getStatus(),
                seats,
                hall.getCreatedAt(),
                hall.getUpdatedAt()
        );
    }
}