package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaBookingEventOutboxRepository extends JpaRepository<BookingEventOutboxJpaEntity, Long> {
    List<BookingEventOutboxJpaEntity> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);
}
