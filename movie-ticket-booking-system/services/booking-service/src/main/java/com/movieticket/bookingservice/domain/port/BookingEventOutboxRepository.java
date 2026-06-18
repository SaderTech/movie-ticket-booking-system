package com.movieticket.bookingservice.domain.port;

import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;

import java.util.List;
import java.util.Optional;

public interface BookingEventOutboxRepository {
    BookingEventOutbox save(BookingEventOutbox outbox);
    Optional<BookingEventOutbox> findById(Long id);
    List<BookingEventOutbox> findPendingEvents(OutboxStatus status, int limit);
}
