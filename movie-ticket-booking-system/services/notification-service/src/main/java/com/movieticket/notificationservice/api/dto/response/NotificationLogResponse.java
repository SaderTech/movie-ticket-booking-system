package com.movieticket.notificationservice.api.dto.response;

import com.movieticket.notificationservice.domain.model.NotificationLog;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationLogResponse(
        UUID id,
        String recipientEmail,
        String subject,
        String message,
        String channel,
        String type,
        String status,
        String errorMessage,
        LocalDateTime createdAt
) {
    public static NotificationLogResponse from(NotificationLog log) {
        return new NotificationLogResponse(
                log.getId(),
                log.getRecipientEmail(),
                log.getSubject(),
                log.getMessage(),
                log.getChannel().name(),
                log.getType().name(),
                log.getStatus().name(),
                log.getErrorMessage(),
                log.getCreatedAt()
        );
    }
}