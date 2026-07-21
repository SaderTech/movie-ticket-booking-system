package com.movieticket.bookingservice.application.mapper;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.dto.TicketResponse;
import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.Payment;
import com.movieticket.bookingservice.domain.entity.Ticket;

import java.util.List;
import java.util.stream.Collectors;

public class BookingResponseMapper {

    public static BookingResponse toResponse(Booking booking, List<Ticket> tickets, Payment payment) {
        List<BookingResponse.BookingSeatDto> seatDtos = booking.getSeats().stream()
                .map(s -> BookingResponse.BookingSeatDto.builder()
                        .seatCode(s.getSeatCode())
                        .seatType(s.getSeatType())
                        .price(s.getPrice())
                        .status(s.getStatus() != null ? s.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());

        List<TicketResponse> ticketDtos = tickets.stream()
                .map(t -> TicketResponse.builder()
                        .id(t.getId())
                        .ticketCode(t.getTicketCode())
                        .bookingId(t.getBookingId())
                        .userId(t.getUserId())
                        .showtimeId(t.getShowtimeId())
                        .movieId(t.getMovieId())
                        .movieTitle(t.getMovieTitle())
                        .cinemaId(t.getCinemaId())
                        .cinemaName(t.getCinemaName())
                        .hallId(t.getHallId())
                        .hallName(t.getHallName())
                        .seatCode(t.getSeatCode())
                        .seatType(t.getSeatType())
                        .showDate(t.getShowDate())
                        .startTime(t.getStartTime())
                        .endTime(t.getEndTime())
                        .price(t.getPrice())
                        .qrPayload(t.getQrPayload())
                        .status(t.getStatus() != null ? t.getStatus().name() : null)
                        .issuedAt(t.getIssuedAt())
                        .build())
                .collect(Collectors.toList());

        BookingResponse.PaymentDto paymentDto = null;
        if (payment != null) {
            paymentDto = BookingResponse.PaymentDto.builder()
                    .id(payment.getId())
                    .transactionRef(payment.getTransactionRef())
                    .method(payment.getMethod())
                    .amount(payment.getAmount())
                    .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                    .paidAt(payment.getPaidAt())
                    .build();
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .showtimeId(booking.getShowtimeId())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .holdToken(booking.getHoldToken())
                .seats(seatDtos)
                .tickets(ticketDtos)
                .payment(paymentDto)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public static BookingResponse toResponse(Booking booking, List<Ticket> tickets) {
        return toResponse(booking, tickets, null);
    }

    public static BookingResponse toVnPayPendingResponse(Booking booking, Payment payment, String paymentUrl) {
        List<BookingResponse.BookingSeatDto> seatDtos = booking.getSeats().stream()
                .map(s -> BookingResponse.BookingSeatDto.builder()
                        .seatCode(s.getSeatCode())
                        .seatType(s.getSeatType())
                        .price(s.getPrice())
                        .status(s.getStatus() != null ? s.getStatus().name() : null)
                        .build())
                .collect(Collectors.toList());

        BookingResponse.PaymentDto paymentDto = null;
        if (payment != null) {
            paymentDto = BookingResponse.PaymentDto.builder()
                    .id(payment.getId())
                    .transactionRef(payment.getTransactionRef())
                    .method(payment.getMethod())
                    .amount(payment.getAmount())
                    .status(payment.getStatus() != null ? payment.getStatus().name() : null)
                    .paidAt(payment.getPaidAt())
                    .build();
        }

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .showtimeId(booking.getShowtimeId())
                .totalAmount(booking.getTotalAmount())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .holdToken(booking.getHoldToken())
                .seats(seatDtos)
                .tickets(List.of())
                .payment(paymentDto)
                .paymentUrl(paymentUrl)
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
