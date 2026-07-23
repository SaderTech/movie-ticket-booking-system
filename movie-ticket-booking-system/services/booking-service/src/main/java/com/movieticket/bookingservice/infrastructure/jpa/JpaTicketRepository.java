package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JpaTicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByTicketCode(String ticketCode);
    List<Ticket> findByBookingId(Long bookingId);
    List<Ticket> findByUserId(Long userId);
    boolean existsByShowtimeIdAndSeatCodeAndStatusIn(Long showtimeId, String seatCode, Collection<TicketStatus> statuses);

    @Query("SELECT t.seatCode FROM Ticket t WHERE t.showtimeId = :showtimeId AND t.status IN :statuses")
    List<String> findUnavailableSeatCodesByShowtimeId(Long showtimeId, Collection<TicketStatus> statuses);
}
