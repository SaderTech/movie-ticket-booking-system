package com.movieticket.bookingservice.infrastructure.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.event.DomainEvent;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventPublisherImpl {

    private final JpaBookingEventOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(DomainEvent event) {
        BookingEventOutbox outbox = mapToOutbox(event);
        outboxRepository.save(outbox);
        log.debug("Published domain event {} to outbox: eventId={}", event.eventType(), event.eventId());
    }

    public void publishAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) return;
        events.stream()
                .map(this::mapToOutbox)
                .forEach(outboxRepository::save);
        log.debug("Published {} domain events to outbox", events.size());
    }

    private BookingEventOutbox mapToOutbox(DomainEvent event) {
        String aggregateType;
        String topic;

        switch (event.eventType()) {
            case "SEAT_HOLD_CREATED" -> {
                aggregateType = "HOLD";
                topic = "booking.seat-hold.created";
            }
            case "SEAT_HOLD_EXPIRED" -> {
                aggregateType = "HOLD";
                topic = "booking.seat-hold.expired";
            }
            case "BOOKING_CONFIRMED" -> {
                aggregateType = "BOOKING";
                topic = "booking.confirmed";
            }
            case "TICKET_BOOKED" -> {
                aggregateType = "TICKET";
                topic = "booking.ticket-booked";
            }
            case "BOOKING_CANCELLED" -> {
                aggregateType = "BOOKING";
                topic = "booking.cancelled";
            }
            case "PAYMENT_REFUND_REQUIRED" -> {
                aggregateType = "PAYMENT";
                topic = "booking.payment.refund-required";
            }
            default -> {
                log.warn("Unknown domain event type: {}, falling back to generic", event.eventType());
                aggregateType = "UNKNOWN";
                topic = "booking.event";
            }
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            log.warn("Failed to serialize domain event {}: {}", event.eventId(), e.getMessage());
            payload = "{\"eventId\":\"" + event.eventId() + "\",\"eventType\":\"" + event.eventType() + "\"}";
        }

        return BookingEventOutbox.builder()
                .eventId(event.eventId())
                .aggregateType(aggregateType)
                .aggregateId(event.aggregateId())
                .bookingId(null)
                .eventType(event.eventType())
                .topic(topic)
                .payloadJson(payload)
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
