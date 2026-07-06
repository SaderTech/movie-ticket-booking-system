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
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final SendNotificationUseCase sendNotificationUseCase;

    @KafkaListener(topics = "payment-success", groupId = "notification-service")
    public void consumePaymentSuccessEvent(String message) {
        try {
            JsonNode jsonNode = objectMapper.readTree(message);

            String customerEmail = jsonNode.path("customerEmail").asText();
            String paymentCode = jsonNode.path("paymentCode").asText();
            String amount = jsonNode.path("amount").asText();

            SendNotificationCommand command = new SendNotificationCommand(
                    customerEmail,
                    "Payment Successful",
                    "Your payment " + paymentCode + " with amount " + amount + " has been completed successfully.",
                    "EMAIL",
                    "PAYMENT_SUCCESS"
            );

            sendNotificationUseCase.execute(command);
        } catch (Exception e) {
            System.out.println("Failed to consume payment-success event: " + e.getMessage());
        }
    }
}