package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaBookingEventOutboxRepository extends JpaRepository<BookingEventOutbox, Long> {
    List<BookingEventOutbox> findByStatusOrderByCreatedAtAsc(OutboxStatus status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM BookingEventOutbox e WHERE e.status = 'PUBLISHED' AND e.createdAt < :cutoff")
    void deletePublishedBefore(LocalDateTime cutoff);
}