package com.movieticket.bookingservice.domain.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record TicketBookedEvent(
        String eventId,
        String bookingCode,
        Long userId,
        String customerEmail,
        String customerName,
        Long showtimeId,
        String movieTitle,
        LocalDate showDate,
        LocalTime startTime,
        BigDecimal totalAmount,
        List<TicketInfo> tickets,
        LocalDateTime occurredAt
) implements DomainEvent {

    public TicketBookedEvent(String bookingCode, Long userId, Long showtimeId,
                             BigDecimal totalAmount, List<TicketInfo> tickets) {
        this(UUID.randomUUID().toString(), bookingCode, userId, null, null, showtimeId,
                null, null, null, totalAmount, tickets == null ? List.of() : List.copyOf(tickets), LocalDateTime.now());
    }

    public TicketBookedEvent(String bookingCode, Long userId, String customerEmail, String customerName,
                             Long showtimeId, BigDecimal totalAmount, List<TicketInfo> tickets) {
        this(UUID.randomUUID().toString(), bookingCode, userId, customerEmail, customerName, showtimeId,
                null, null, null, totalAmount, tickets == null ? List.of() : List.copyOf(tickets), LocalDateTime.now());
    }

    public TicketBookedEvent(String bookingCode, Long userId, String customerEmail, String customerName,
                             Long showtimeId, String movieTitle, LocalDate showDate, LocalTime startTime,
                             BigDecimal totalAmount, List<TicketInfo> tickets) {
        this(UUID.randomUUID().toString(), bookingCode, userId, customerEmail, customerName, showtimeId,
                movieTitle, showDate, startTime, totalAmount, tickets == null ? List.of() : List.copyOf(tickets), LocalDateTime.now());
    }

    @Override
    public String aggregateId() {
        return bookingCode;
    }

    @Override
    public String eventType() {
        return "TICKET_BOOKED";
    }

    public record TicketInfo(String ticketCode, String seatCode) {}
}
