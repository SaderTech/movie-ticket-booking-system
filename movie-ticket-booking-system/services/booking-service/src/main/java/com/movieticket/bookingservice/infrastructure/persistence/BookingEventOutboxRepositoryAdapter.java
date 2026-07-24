package com.movieticket.bookingservice.infrastructure.persistence;

import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.repository.BookingEventOutboxRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingEventOutboxRepositoryAdapter implements BookingEventOutboxRepository {
    private final JpaBookingEventOutboxRepository jpaRepository;
    public BookingEventOutbox save(BookingEventOutbox outboxEvent) { return jpaRepository.save(outboxEvent); }
}
