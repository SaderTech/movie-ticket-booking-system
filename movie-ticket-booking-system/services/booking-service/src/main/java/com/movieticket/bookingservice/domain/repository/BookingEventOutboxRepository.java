package com.movieticket.bookingservice.domain.repository;

import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;

public interface BookingEventOutboxRepository {
    BookingEventOutbox save(BookingEventOutbox outboxEvent);
}
