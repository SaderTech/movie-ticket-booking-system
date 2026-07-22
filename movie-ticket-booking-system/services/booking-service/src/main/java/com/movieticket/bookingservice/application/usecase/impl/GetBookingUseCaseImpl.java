package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.mapper.BookingResponseMapper;
import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetBookingUseCaseImpl {

    private final JpaBookingRepository bookingRepository;
    private final JpaTicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public BookingResponse findByBookingCode(String bookingCode, Long userId) {
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, 401, "Missing user authentication");
        }
        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOKING_NOT_FOUND, "Booking not found: " + bookingCode));
        if (!booking.getUserId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, 403,
                    "You can only view your own bookings");
        }
        List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
        return BookingResponseMapper.toResponse(booking, tickets);
    }
}