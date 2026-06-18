package com.movieticket.bookingservice.infrastructure.mapper;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.BookingSeat;
import com.movieticket.bookingservice.infrastructure.jpa.BookingJpaEntity;
import com.movieticket.bookingservice.infrastructure.jpa.BookingSeatJpaEntity;

import java.util.stream.Collectors;

public class BookingMapper {

    public static Booking toDomain(BookingJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        Booking booking = Booking.builder()
                .id(entity.getId())
                .bookingCode(entity.getBookingCode())
                .userId(entity.getUserId())
                .showtimeId(entity.getShowtimeId())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .holdToken(entity.getHoldToken())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();

        if (entity.getSeats() != null) {
            booking.setSeats(entity.getSeats().stream()
                    .map(BookingMapper::toDomainSeat)
                    .collect(Collectors.toList()));
        }
        return booking;
    }

    public static BookingJpaEntity toEntity(Booking domain) {
        if (domain == null) {
            return null;
        }
        BookingJpaEntity entity = new BookingJpaEntity();
        entity.setId(domain.getId());
        entity.setBookingCode(domain.getBookingCode());
        entity.setUserId(domain.getUserId());
        entity.setShowtimeId(domain.getShowtimeId());
        entity.setTotalAmount(domain.getTotalAmount());
        entity.setStatus(domain.getStatus());
        entity.setHoldToken(domain.getHoldToken());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        if (domain.getSeats() != null) {
            entity.setSeats(domain.getSeats().stream()
                    .map(seat -> toEntitySeat(seat, entity))
                    .collect(Collectors.toList()));
        }
        return entity;
    }

    public static BookingSeat toDomainSeat(BookingSeatJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return BookingSeat.builder()
                .id(entity.getId())
                .bookingId(entity.getBooking() != null ? entity.getBooking().getId() : null)
                .showtimeId(entity.getShowtimeId())
                .seatCode(entity.getSeatCode())
                .seatType(entity.getSeatType())
                .price(entity.getPrice())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static BookingSeatJpaEntity toEntitySeat(BookingSeat domain, BookingJpaEntity bookingEntity) {
        if (domain == null) {
            return null;
        }
        BookingSeatJpaEntity entity = new BookingSeatJpaEntity();
        entity.setId(domain.getId());
        entity.setBooking(bookingEntity);
        entity.setShowtimeId(domain.getShowtimeId());
        entity.setSeatCode(domain.getSeatCode());
        entity.setSeatType(domain.getSeatType());
        entity.setPrice(domain.getPrice());
        entity.setStatus(domain.getStatus());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }
}
