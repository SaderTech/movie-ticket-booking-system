package com.movieticket.notificationservice.application.command;

public record SendNotificationCommand(
        String recipientEmail,
        String subject,
        String message,
        String channel,
        String type
) {
}