package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.application.mapper.BookingResponseMapper;
import com.movieticket.bookingservice.application.usecase.GetMyBookingsUseCase;
import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.domain.port.BookingRepository;
import com.movieticket.bookingservice.domain.port.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetMyBookingsUseCaseImpl implements GetMyBookingsUseCase {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse> findByUserId(Long userId) {
        List<Booking> bookings = bookingRepository.findByUserId(userId);
        return bookings.stream()
                .map(b -> {
                    List<Ticket> tickets = ticketRepository.findByBookingId(b.getId());
                    return BookingResponseMapper.toResponse(b, tickets);
                })
                .collect(Collectors.toList());
    }
}
