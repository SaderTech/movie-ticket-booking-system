package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.SeatAvailabilityResponse;
import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSeatHoldRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetSeatAvailabilityUseCaseImpl {

    private final JpaSeatHoldRepository seatHoldRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaBookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public SeatAvailabilityResponse findByShowtimeId(Long showtimeId) {
        LocalDateTime now = LocalDateTime.now();
        Set<String> heldSeats = new HashSet<>(seatHoldRepository.findActiveSeatCodesByShowtimeId(showtimeId, now));
        Set<String> bookedSeats = new HashSet<>(ticketRepository.findUnavailableSeatCodesByShowtimeId(
                showtimeId, List.of(TicketStatus.ACTIVE, TicketStatus.USED)));
        bookedSeats.addAll(bookingRepository.findSeatCodesByShowtimeIdAndStatusIn(
                showtimeId, List.of(BookingSeatStatus.PENDING, BookingSeatStatus.CONFIRMED)));

        heldSeats.removeAll(bookedSeats);
        return SeatAvailabilityResponse.builder()
                .showtimeId(showtimeId)
                .heldSeatCodes(heldSeats.stream().sorted().toList())
                .bookedSeatCodes(bookedSeats.stream().sorted().toList())
                .build();
    }
}
