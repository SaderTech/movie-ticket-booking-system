package com.movieticket.bookingservice.domain.repository;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.BookingSeat;
import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(Long id);
    Optional<Booking> findByBookingCode(String bookingCode);
    Optional<Booking> findByHoldToken(String holdToken);
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    boolean existsPendingBookingForSeat(Long showtimeId, String seatCode, Collection<BookingSeatStatus> statuses);
    List<String> findSeatCodesByShowtimeIdAndStatusIn(Long showtimeId, Collection<BookingSeatStatus> statuses);
}
