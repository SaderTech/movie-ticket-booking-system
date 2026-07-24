package com.movieticket.bookingservice.domain.repository;

import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    List<Ticket> saveAll(Iterable<Ticket> tickets);
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByBookingId(Long bookingId);
    boolean existsByShowtimeIdAndSeatCodeAndStatusIn(Long showtimeId, String seatCode, Collection<TicketStatus> statuses);
    List<String> findUnavailableSeatCodesByShowtimeId(Long showtimeId, Collection<TicketStatus> statuses);
}
