package com.movieticket.bookingservice.domain.repository;

import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SeatHoldRepository {
    SeatHold save(SeatHold seatHold);
    Optional<SeatHold> findByHoldToken(String holdToken);
    boolean existsActiveHoldForSeat(Long showtimeId, String seatCode, LocalDateTime now);
    List<String> findActiveSeatCodesByShowtimeId(Long showtimeId, LocalDateTime now);
}
