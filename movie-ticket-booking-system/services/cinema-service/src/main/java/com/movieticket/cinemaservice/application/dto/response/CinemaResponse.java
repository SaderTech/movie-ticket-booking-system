package com.movieticket.cinemaservice.application.dto.response;

import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CinemaResponse(
        Long id,
        String name,
        String address,
        String city,
        String contactPhone,
        BigDecimal latitude,
        BigDecimal longitude,
        CinemaStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static CinemaResponse from(Cinema cinema) {
        return new CinemaResponse(
                cinema.getId(),
                cinema.getName(),
                cinema.getAddress(),
                cinema.getCity(),
                cinema.getContactPhone(),
                cinema.getLatitude(),
                cinema.getLongitude(),
                cinema.getStatus(),
                cinema.getCreatedAt(),
                cinema.getUpdatedAt()
        );
    }
}
