package com.movieticket.notificationservice.api.dto.response;

import com.movieticket.notificationservice.domain.entity.NotificationLog;

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
        String sourceEventId,
        String sourceTopic,
        int retryCount,
        int maxRetries,
        LocalDateTime nextRetryAt,
        LocalDateTime scheduledAt,
        LocalDateTime createdAt,
        LocalDateTime sentAt
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
                log.getSourceEventId(),
                log.getSourceTopic(),
                log.getRetryCount(),
                log.getMaxRetries(),
                log.getNextRetryAt(),
                log.getScheduledAt(),
                log.getCreatedAt(),
                log.getSentAt()
        );
    }
}
