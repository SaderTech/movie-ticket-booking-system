package com.movieticket.bookingservice.infrastructure.persistence;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import com.movieticket.bookingservice.domain.repository.BookingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingRepository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {
    private final JpaBookingRepository jpaRepository;
    public Booking save(Booking booking) { return jpaRepository.save(booking); }
    public Optional<Booking> findById(Long id) { return jpaRepository.findById(id); }
    public Optional<Booking> findByBookingCode(String code) { return jpaRepository.findByBookingCode(code); }
    public Optional<Booking> findByHoldToken(String token) { return jpaRepository.findByHoldToken(token); }
    public Page<Booking> findByUserId(Long userId, Pageable pageable) { return jpaRepository.findByUserId(userId, pageable); }
    public boolean existsPendingBookingForSeat(Long showtimeId, String seatCode, Collection<BookingSeatStatus> statuses) { return jpaRepository.existsPendingBookingForSeat(showtimeId, seatCode, statuses); }
    public List<String> findSeatCodesByShowtimeIdAndStatusIn(Long showtimeId, Collection<BookingSeatStatus> statuses) { return jpaRepository.findSeatCodesByShowtimeIdAndStatusIn(showtimeId, statuses); }
}
