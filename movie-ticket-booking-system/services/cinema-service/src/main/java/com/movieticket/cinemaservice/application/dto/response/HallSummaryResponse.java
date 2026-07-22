package com.movieticket.cinemaservice.application.dto.response;

import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.HallType;

public record HallSummaryResponse(
        Long id,
        Long cinemaId,
        String cinemaName,
        String name,
        Integer capacity,
        HallType hallType,
        HallStatus status
) {

    public static HallSummaryResponse from(Hall hall) {
        return new HallSummaryResponse(
                hall.getId(),
                hall.getCinema().getId(),
                hall.getCinema().getName(),
                hall.getName(),
                hall.getCapacity(),
                hall.getHallType(),
                hall.getStatus()
        );
    }
}
