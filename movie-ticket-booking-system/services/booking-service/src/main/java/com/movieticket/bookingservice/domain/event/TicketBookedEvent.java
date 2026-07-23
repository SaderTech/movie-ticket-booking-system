package com.movieticket.bookingservice.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TicketBookedEvent(
        String eventId,
        String bookingCode,
        Long userId,
        String customerEmail,
        String customerName,
        Long showtimeId,
        BigDecimal totalAmount,
        List<TicketInfo> tickets,
        LocalDateTime occurredAt
) implements DomainEvent {

    public TicketBookedEvent(String bookingCode, Long userId, Long showtimeId,
                             BigDecimal totalAmount, List<TicketInfo> tickets) {
        this(UUID.randomUUID().toString(), bookingCode, userId, null, null, showtimeId,
                totalAmount, tickets, LocalDateTime.now());
    }

    public TicketBookedEvent(String bookingCode, Long userId, String customerEmail, String customerName,
                             Long showtimeId, BigDecimal totalAmount, List<TicketInfo> tickets) {
        this(UUID.randomUUID().toString(), bookingCode, userId, customerEmail, customerName, showtimeId,
                totalAmount, tickets, LocalDateTime.now());
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
