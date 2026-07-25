package com.movieticket.bookingservice.infrastructure.persistence;

import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import com.movieticket.bookingservice.domain.repository.TicketRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaTicketRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TicketRepositoryAdapter implements TicketRepository {
    private final JpaTicketRepository jpaRepository;
    public Ticket save(Ticket ticket) { return jpaRepository.save(ticket); }
    public List<Ticket> saveAll(Iterable<Ticket> tickets) { return jpaRepository.saveAll(tickets); }
    public Optional<Ticket> findByTicketCode(String code) { return jpaRepository.findByTicketCode(code); }
    public List<Ticket> findByBookingId(Long bookingId) { return jpaRepository.findByBookingId(bookingId); }
    public boolean existsByShowtimeIdAndSeatCodeAndStatusIn(Long showtimeId, String seatCode, Collection<TicketStatus> statuses) { return jpaRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(showtimeId, seatCode, statuses); }
    public List<String> findUnavailableSeatCodesByShowtimeId(Long showtimeId, Collection<TicketStatus> statuses) { return jpaRepository.findUnavailableSeatCodesByShowtimeId(showtimeId, statuses); }
}
