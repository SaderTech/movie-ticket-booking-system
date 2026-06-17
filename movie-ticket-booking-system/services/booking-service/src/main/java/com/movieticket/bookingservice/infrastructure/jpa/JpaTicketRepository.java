package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JpaTicketRepository extends JpaRepository<TicketJpaEntity, Long> {
    Optional<TicketJpaEntity> findByTicketCode(String ticketCode);
    List<TicketJpaEntity> findByBookingId(Long bookingId);
    List<TicketJpaEntity> findByUserId(Long userId);
    boolean existsByShowtimeIdAndSeatCodeAndStatusIn(Long showtimeId, String seatCode, Collection<TicketStatus> statuses);
}
