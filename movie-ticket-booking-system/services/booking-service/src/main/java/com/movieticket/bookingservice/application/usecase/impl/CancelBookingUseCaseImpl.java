package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.command.CancelBookingCommand;
import com.movieticket.bookingservice.application.mapper.BookingResponseMapper;
import com.movieticket.bookingservice.application.usecase.CancelBookingUseCase;
import com.movieticket.bookingservice.domain.aggregate.BookingAggregate;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.port.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelBookingUseCaseImpl implements CancelBookingUseCase {

    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final BookingEventOutboxRepository outboxRepository;

    @Override
    @Transactional
    public BookingResponse execute(CancelBookingCommand command) {
        Booking booking = bookingRepository.findByBookingCode(command.getBookingCode())
                .orElseThrow(() -> new ApiException(ErrorCode.BOOKING_NOT_FOUND,
                        "Booking not found: " + command.getBookingCode()));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(ErrorCode.BOOKING_CANNOT_BE_CANCELLED,
                    "Booking is already cancelled");
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);

        BookingAggregate aggregate = BookingAggregate.forExistingCancel(booking, tickets, payment);
        aggregate.cancelBooking(command.getReason());

        booking = bookingRepository.save(aggregate.getBooking());
        if (!aggregate.getTickets().isEmpty()) {
            ticketRepository.saveAll(aggregate.getTickets());
        }
        if (aggregate.getPayment() != null) {
            paymentRepository.save(aggregate.getPayment());
        }

        BookingEventOutbox outbox = BookingEventOutbox.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType("Booking")
                .aggregateId(command.getBookingCode())
                .bookingId(booking.getId())
                .eventType("BOOKING_CANCELLED")
                .topic("booking.cancelled")
                .payloadJson("{\"bookingCode\":\"" + command.getBookingCode()
                        + "\",\"userId\":" + booking.getUserId()
                        + ",\"showtimeId\":" + booking.getShowtimeId()
                        + ",\"reason\":\"" + (command.getReason() != null ? command.getReason() : "") + "\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
        outboxRepository.save(outbox);

        return BookingResponseMapper.toResponse(booking, tickets);
    }
}
