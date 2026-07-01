package com.movieticket.bookingservice.domain.port;

import com.movieticket.bookingservice.domain.entity.SeatHold;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatHoldRepository {
    SeatHold save(SeatHold seatHold);
    Optional<SeatHold> findById(Long id);
    Optional<SeatHold> findByHoldToken(String holdToken);
    List<SeatHold> findExpiredHolds(LocalDateTime now);
    boolean existsActiveHoldForSeat(Long showtimeId, String seatCode, LocalDateTime now);
}
