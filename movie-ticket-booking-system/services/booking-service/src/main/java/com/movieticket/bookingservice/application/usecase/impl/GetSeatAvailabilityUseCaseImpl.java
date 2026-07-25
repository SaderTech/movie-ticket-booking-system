package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.SeatAvailabilityResponse;
import com.movieticket.bookingservice.application.usecase.GetSeatAvailabilityUseCase;
import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import com.movieticket.bookingservice.domain.repository.BookingRepository;
import com.movieticket.bookingservice.domain.repository.SeatHoldRepository;
import com.movieticket.bookingservice.domain.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GetSeatAvailabilityUseCaseImpl implements GetSeatAvailabilityUseCase {

    private final SeatHoldRepository seatHoldRepository;
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;

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
