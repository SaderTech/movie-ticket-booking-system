package com.movieticket.bookingservice.infrastructure.kafka;

import com.movieticket.bookingservice.infrastructure.kafka.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
@RequiredArgsConstructor
@Slf4j
public class BookingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSeatHoldCreated(SeatHoldCreatedEvent event) {
        kafkaTemplate.send("booking.seat-hold.created", event.getHoldToken(), event);
        log.debug("Published seat hold created event: {}", event.getHoldToken());
    }

    public void publishSeatHoldExpired(SeatHoldExpiredEvent event) {
        kafkaTemplate.send("booking.seat-hold.expired", event.getHoldToken(), event);
        log.debug("Published seat hold expired event: {}", event.getHoldToken());
    }

    public void publishBookingConfirmed(BookingConfirmedEvent event) {
        kafkaTemplate.send("booking.confirmed", event.getBookingCode(), event);
        log.debug("Published booking confirmed event: {}", event.getBookingCode());
    }

    public void publishTicketBooked(TicketBookedEvent event) {
        kafkaTemplate.send("booking.ticket-booked", event.getBookingCode(), event);
        log.debug("Published ticket booked event: {}", event.getBookingCode());
    }

    public void publishBookingCancelled(BookingCancelledEvent event) {
        kafkaTemplate.send("booking.cancelled", event.getBookingCode(), event);
        log.debug("Published booking cancelled event: {}", event.getBookingCode());
    }
}
