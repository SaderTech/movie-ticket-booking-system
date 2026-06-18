package com.movieticket.bookingservice.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JpaBookingRepository extends JpaRepository<BookingJpaEntity, Long> {
    Optional<BookingJpaEntity> findByBookingCode(String bookingCode);
    List<BookingJpaEntity> findByUserId(Long userId);
}
