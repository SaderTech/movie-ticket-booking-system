package com.movieticket.notificationservice.infrastructure.messaging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.application.usecase.SendNotificationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingEventConsumer {

    private final ObjectMapper objectMapper;
    private final SendNotificationUseCase sendNotificationUseCase;

    @KafkaListener(topics = "booking-created", groupId = "notification-service")
    public void consumeBookingCreatedEvent(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            String customerEmail = jsonNode.path("customerEmail").asText();
            String bookingCode = jsonNode.path("bookingCode").asText();
            String movieTitle = jsonNode.path("movieTitle").asText();

            SendNotificationCommand command = new SendNotificationCommand(
                    customerEmail,
                    "Booking Confirmation",
                    "Your booking " + bookingCode + " for movie " + movieTitle + " has been created successfully.",
                    "EMAIL",
                    "BOOKING_CONFIRMATION"
            );

            sendNotificationUseCase.execute(command);
        } catch (Exception e) {
            System.out.println("Failed to consume booking-created event: " + e.getMessage());
        }
    }
}