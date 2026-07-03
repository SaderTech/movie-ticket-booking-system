package com.movieticket.bookingservice.domain.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    String eventId();
    String aggregateId();
    String eventType();
    LocalDateTime occurredAt();
}
