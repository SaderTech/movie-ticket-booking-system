package com.movieticket.bookingservice.infrastructure.mapper;

import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.entity.SeatHoldSeat;
import com.movieticket.bookingservice.infrastructure.jpa.SeatHoldJpaEntity;
import com.movieticket.bookingservice.infrastructure.jpa.SeatHoldSeatJpaEntity;

import java.util.stream.Collectors;

public class SeatHoldMapper {

    public static SeatHold toDomain(SeatHoldJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        SeatHold hold = SeatHold.builder()
                .id(entity.getId())
                .holdToken(entity.getHoldToken())
                .userId(entity.getUserId())
                .showtimeId(entity.getShowtimeId())
                .status(entity.getStatus())
                .expiresAt(entity.getExpiresAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        if (entity.getSeats() != null) {
            hold.setSeats(entity.getSeats().stream()
                    .map(SeatHoldMapper::toDomainSeat)
                    .collect(Collectors.toList()));
        }
        return hold;
    }

    public static SeatHoldJpaEntity toEntity(SeatHold domain) {
        if (domain == null) {
            return null;
        }
        SeatHoldJpaEntity entity = new SeatHoldJpaEntity();
        entity.setId(domain.getId());
        entity.setHoldToken(domain.getHoldToken());
        entity.setUserId(domain.getUserId());
        entity.setShowtimeId(domain.getShowtimeId());
        entity.setStatus(domain.getStatus());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        if (domain.getSeats() != null) {
            entity.setSeats(domain.getSeats().stream()
                    .map(seat -> toEntitySeat(seat, entity))
                    .collect(Collectors.toList()));
        }
        return entity;
    }

    public static SeatHoldSeat toDomainSeat(SeatHoldSeatJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return SeatHoldSeat.builder()
                .id(entity.getId())
                .holdId(entity.getSeatHold() != null ? entity.getSeatHold().getId() : null)
                .showtimeId(entity.getShowtimeId())
                .seatCode(entity.getSeatCode())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static SeatHoldSeatJpaEntity toEntitySeat(SeatHoldSeat domain, SeatHoldJpaEntity holdEntity) {
        if (domain == null) {
            return null;
        }
        SeatHoldSeatJpaEntity entity = new SeatHoldSeatJpaEntity();
        entity.setId(domain.getId());
        entity.setSeatHold(holdEntity);
        entity.setShowtimeId(domain.getShowtimeId());
        entity.setSeatCode(domain.getSeatCode());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
