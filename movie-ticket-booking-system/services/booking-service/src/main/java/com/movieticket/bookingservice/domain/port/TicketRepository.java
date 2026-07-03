package com.movieticket.bookingservice.domain.port;

import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface TicketRepository {
    Ticket save(Ticket ticket);
    List<Ticket> saveAll(List<Ticket> tickets);
    Optional<Ticket> findById(Long id);
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByBookingId(Long bookingId);
    List<Ticket> findByUserId(Long userId);
    boolean existsByShowtimeIdAndSeatCodeAndStatusIn(Long showtimeId, String seatCode, Collection<TicketStatus> statuses);
}
