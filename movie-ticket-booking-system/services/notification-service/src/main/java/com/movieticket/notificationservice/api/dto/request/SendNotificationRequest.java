package com.movieticket.notificationservice.api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendNotificationRequest(
        @NotBlank(message = "Recipient email is required")
        @Email(message = "Recipient email is invalid")
        String recipientEmail,

        @NotBlank(message = "Subject is required")
        String subject,

        @NotBlank(message = "Message is required")
        String message,

        String channel,

        String type
) {
}