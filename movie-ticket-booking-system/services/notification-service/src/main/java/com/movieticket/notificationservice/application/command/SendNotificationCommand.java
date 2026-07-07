package com.movieticket.notificationservice.application.command;

import java.time.LocalDateTime;

public record SendNotificationCommand(
        String recipientEmail,
        String subject,
        String message,
        String channel,
        String type,
        String sourceEventId,
        String sourceTopic,
        LocalDateTime scheduledAt
) {
    public SendNotificationCommand(
            String recipientEmail,
            String subject,
            String message,
            String channel,
            String type
    ) {
        this(recipientEmail, subject, message, channel, type, null, null, null);
    }
}
