package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.BookingSeat;
import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface JpaBookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);
    Optional<Booking> findByHoldToken(String holdToken);
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    long countByUserId(Long userId);

    @Query("SELECT COUNT(bs) > 0 FROM BookingSeat bs WHERE bs.showtimeId = :showtimeId AND bs.seatCode = :seatCode AND bs.status IN :statuses")
    boolean existsPendingBookingForSeat(Long showtimeId, String seatCode, Collection<BookingSeatStatus> statuses);

    @Query("SELECT bs.seatCode FROM BookingSeat bs WHERE bs.showtimeId = :showtimeId AND bs.status IN :statuses")
    List<String> findSeatCodesByShowtimeIdAndStatusIn(Long showtimeId, Collection<BookingSeatStatus> statuses);
}
