package com.movieticket.bookingservice.infrastructure.persistence;

import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.repository.SeatHoldRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSeatHoldRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SeatHoldRepositoryAdapter implements SeatHoldRepository {
    private final JpaSeatHoldRepository jpaRepository;
    public SeatHold save(SeatHold hold) { return jpaRepository.save(hold); }
    public Optional<SeatHold> findByHoldToken(String token) { return jpaRepository.findByHoldToken(token); }
    public boolean existsActiveHoldForSeat(Long showtimeId, String seatCode, LocalDateTime now) { return jpaRepository.existsActiveHoldForSeat(showtimeId, seatCode, now); }
    public List<String> findActiveSeatCodesByShowtimeId(Long showtimeId, LocalDateTime now) { return jpaRepository.findActiveSeatCodesByShowtimeId(showtimeId, now); }
}
