package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.dto.TicketResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.command.CancelBookingCommand;
import com.movieticket.bookingservice.application.usecase.CancelBookingUseCase;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.port.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        if (booking.getStatus() != BookingStatus.CONFIRMED && booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new ApiException(ErrorCode.BOOKING_CANNOT_BE_CANCELLED,
                    "Booking cannot be cancelled in status: " + booking.getStatus());
        }

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setUpdatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
        tickets.forEach(t -> {
            t.setStatus(TicketStatus.CANCELLED);
            t.setUpdatedAt(LocalDateTime.now());
        });
        if (!tickets.isEmpty()) {
            ticketRepository.saveAll(tickets);
        }

        paymentRepository.findByBookingId(booking.getId()).ifPresent(payment -> {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
        });

        BookingEventOutbox outbox = BookingEventOutbox.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType("Booking")
                .aggregateId(command.getBookingCode())
                .bookingId(booking.getId())
                .eventType("BOOKING_CANCELLED")
                .topic("booking.cancelled")
                .payloadJson("{\"bookingCode\":\"" + command.getBookingCode()
                        + "\",\"reason\":\"" + (command.getReason() != null ? command.getReason() : "") + "\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
        outboxRepository.save(outbox);

        return toBookingResponse(booking, tickets);
    }

    private BookingResponse toBookingResponse(Booking booking, List<Ticket> tickets) {
        List<BookingResponse.BookingSeatDto> seatDtos = booking.getSeats().stream()
                .map(s -> BookingResponse.BookingSeatDto.builder()
                        .seatCode(s.getSeatCode())
                        .seatType(s.getSeatType())
                        .price(s.getPrice())
                        .status(s.getStatus().name())
                        .build())
                .collect(Collectors.toList());

        List<TicketResponse> ticketDtos = tickets.stream()
                .map(t -> TicketResponse.builder()
                        .id(t.getId())
                        .ticketCode(t.getTicketCode())
                        .bookingId(t.getBookingId())
                        .userId(t.getUserId())
                        .showtimeId(t.getShowtimeId())
                        .seatCode(t.getSeatCode())
                        .price(t.getPrice())
                        .qrPayload(t.getQrPayload())
                        .status(t.getStatus().name())
                        .issuedAt(t.getIssuedAt())
                        .build())
                .collect(Collectors.toList());

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .showtimeId(booking.getShowtimeId())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus().name())
                .holdToken(booking.getHoldToken())
                .seats(seatDtos)
                .tickets(ticketDtos)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
