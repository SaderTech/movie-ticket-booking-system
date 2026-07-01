package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.dto.TicketResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.command.ConfirmBookingCommand;
import com.movieticket.bookingservice.application.usecase.ConfirmBookingUseCase;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.port.BookingRepository;
import com.movieticket.bookingservice.domain.port.BookingEventOutboxRepository;
import com.movieticket.bookingservice.domain.port.PaymentRepository;
import com.movieticket.bookingservice.domain.port.SeatHoldRepository;
import com.movieticket.bookingservice.domain.port.TicketRepository;
import com.movieticket.bookingservice.infrastructure.adapter.PaymentAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmBookingUseCaseImpl implements ConfirmBookingUseCase {

    private final SeatHoldRepository seatHoldRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final BookingEventOutboxRepository outboxRepository;
    private final PaymentAdapter paymentAdapter;

    @Override
    @Transactional
    public BookingResponse execute(ConfirmBookingCommand command) {
        SeatHold seatHold = seatHoldRepository.findByHoldToken(command.getHoldToken())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_HOLD_TOKEN,
                        "Hold token not found: " + command.getHoldToken()));

        if (seatHold.getStatus() != SeatHoldStatus.ACTIVE) {
            throw new ApiException(ErrorCode.INVALID_HOLD_TOKEN,
                    "Hold token is no longer active (status: " + seatHold.getStatus() + ")");
        }
        if (seatHold.getExpiresAt().isBefore(LocalDateTime.now())) {
            seatHold.setStatus(SeatHoldStatus.EXPIRED);
            seatHoldRepository.save(seatHold);
            throw new ApiException(ErrorCode.HOLD_EXPIRED, "Seat hold has expired");
        }

        String bookingCode = "BK" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        BigDecimal totalAmount = BigDecimal.ZERO;

        List<BookingSeat> bookingSeats = seatHold.getSeats().stream()
                .map(shSeat -> BookingSeat.builder()
                        .showtimeId(seatHold.getShowtimeId())
                        .seatCode(shSeat.getSeatCode())
                        .price(BigDecimal.ZERO)
                        .status(BookingSeatStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        if (!bookingSeats.isEmpty()) {
            totalAmount = bookingSeats.stream()
                    .map(BookingSeat::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .userId(command.getUserId())
                .showtimeId(seatHold.getShowtimeId())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING_PAYMENT)
                .holdToken(command.getHoldToken())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(bookingSeats)
                .build();
        booking = bookingRepository.save(booking);

        final Long bookingId = booking.getId();
        booking.getSeats().forEach(s -> s.setBookingId(bookingId));

        Payment payment = paymentAdapter.processPayment(booking, command.getPaymentMethod());
        payment.setBookingId(bookingId);
        payment = paymentRepository.save(payment);

        if (payment.getStatus() == PaymentStatus.PAID) {
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.getSeats().forEach(s -> s.setStatus(BookingSeatStatus.CONFIRMED));
            bookingRepository.save(booking);

            seatHold.setStatus(SeatHoldStatus.CONVERTED);
            seatHoldRepository.save(seatHold);

            List<Ticket> tickets = bookingSeats.stream()
                    .map(bs -> Ticket.builder()
                            .ticketCode("TCK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase())
                            .bookingId(bookingId)
                            .userId(command.getUserId())
                            .showtimeId(seatHold.getShowtimeId())
                            .seatCode(bs.getSeatCode())
                            .price(bs.getPrice())
                            .status(TicketStatus.ACTIVE)
                            .issuedAt(LocalDateTime.now())
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());
            tickets = ticketRepository.saveAll(tickets);

            BookingEventOutbox outbox = BookingEventOutbox.builder()
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType("Booking")
                    .aggregateId(bookingCode)
                    .bookingId(bookingId)
                    .eventType("BOOKING_CONFIRMED")
                    .topic("booking.confirmed")
                    .payloadJson("{\"bookingCode\":\"" + bookingCode + "\",\"userId\":" + command.getUserId()
                            + ",\"totalAmount\":" + totalAmount + "}")
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxRepository.save(outbox);

            return toBookingResponse(booking, tickets, payment);
        } else {
            booking.setStatus(BookingStatus.FAILED);
            bookingRepository.save(booking);

            seatHold.setStatus(SeatHoldStatus.RELEASED);
            seatHoldRepository.save(seatHold);

            throw new ApiException(ErrorCode.PAYMENT_FAILED,
                    "Payment failed: " + (payment.getFailureReason() != null ? payment.getFailureReason() : "Unknown error"));
        }
    }

    private BookingResponse toBookingResponse(Booking booking, List<Ticket> tickets, Payment payment) {
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

        BookingResponse.PaymentDto paymentDto = BookingResponse.PaymentDto.builder()
                .id(payment.getId())
                .transactionRef(payment.getTransactionRef())
                .method(payment.getMethod())
                .amount(payment.getAmount())
                .status(payment.getStatus().name())
                .paidAt(payment.getPaidAt())
                .build();

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
                .payment(paymentDto)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
