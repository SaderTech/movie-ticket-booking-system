package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface JpaSeatHoldRepository extends JpaRepository<SeatHoldJpaEntity, Long> {
    Optional<SeatHoldJpaEntity> findByHoldToken(String holdToken);
    List<SeatHoldJpaEntity> findByStatusAndExpiresAtBefore(SeatHoldStatus status, LocalDateTime now);
    
    @Query("SELECT COUNT(h) > 0 FROM SeatHoldJpaEntity h JOIN h.seats s WHERE h.showtimeId = :showtimeId AND s.seatCode = :seatCode AND h.status = :status AND h.expiresAt > :now")
    boolean existsActiveHoldForSeat(Long showtimeId, String seatCode, SeatHoldStatus status, LocalDateTime now);
}
