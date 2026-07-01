package com.movieticket.bookingservice.infrastructure.mapper;

import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.infrastructure.jpa.TicketJpaEntity;

public class TicketMapper {

    public static Ticket toDomain(TicketJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Ticket.builder()
                .id(entity.getId())
                .ticketCode(entity.getTicketCode())
                .bookingId(entity.getBookingId())
                .userId(entity.getUserId())
                .showtimeId(entity.getShowtimeId())
                .movieId(entity.getMovieId())
                .movieTitle(entity.getMovieTitle())
                .moviePosterUrl(entity.getMoviePosterUrl())
                .cinemaId(entity.getCinemaId())
                .cinemaName(entity.getCinemaName())
                .hallId(entity.getHallId())
                .hallName(entity.getHallName())
                .seatCode(entity.getSeatCode())
                .seatType(entity.getSeatType())
                .showDate(entity.getShowDate())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .price(entity.getPrice())
                .qrPayload(entity.getQrPayload())
                .status(entity.getStatus())
                .issuedAt(entity.getIssuedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static TicketJpaEntity toEntity(Ticket domain) {
        if (domain == null) {
            return null;
        }
        TicketJpaEntity entity = new TicketJpaEntity();
        entity.setId(domain.getId());
        entity.setTicketCode(domain.getTicketCode());
        entity.setBookingId(domain.getBookingId());
        entity.setUserId(domain.getUserId());
        entity.setShowtimeId(domain.getShowtimeId());
        entity.setMovieId(domain.getMovieId());
        entity.setMovieTitle(domain.getMovieTitle());
        entity.setMoviePosterUrl(domain.getMoviePosterUrl());
        entity.setCinemaId(domain.getCinemaId());
        entity.setCinemaName(domain.getCinemaName());
        entity.setHallId(domain.getHallId());
        entity.setHallName(domain.getHallName());
        entity.setSeatCode(domain.getSeatCode());
        entity.setSeatType(domain.getSeatType());
        entity.setShowDate(domain.getShowDate());
        entity.setStartTime(domain.getStartTime());
        entity.setEndTime(domain.getEndTime());
        entity.setPrice(domain.getPrice());
        entity.setQrPayload(domain.getQrPayload());
        entity.setStatus(domain.getStatus());
        entity.setIssuedAt(domain.getIssuedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
