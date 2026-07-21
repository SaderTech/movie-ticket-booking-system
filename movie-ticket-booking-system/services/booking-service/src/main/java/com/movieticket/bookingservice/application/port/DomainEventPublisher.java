package com.movieticket.bookingservice.application.port;

import com.movieticket.bookingservice.domain.event.DomainEvent;

import java.util.List;

public interface DomainEventPublisher {
    void publish(DomainEvent event);
    void publishAll(List<DomainEvent> events);
}
