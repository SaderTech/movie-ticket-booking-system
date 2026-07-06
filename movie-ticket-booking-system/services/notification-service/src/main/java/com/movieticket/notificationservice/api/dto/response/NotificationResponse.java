package com.movieticket.notificationservice.api.dto.response;

import com.movieticket.notificationservice.domain.entity.NotificationLog;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String recipientEmail,
        String subject,
        String channel,
        String type,
        String status,
        String errorMessage,
        String sourceEventId,
        String sourceTopic,
        int retryCount,
        LocalDateTime nextRetryAt,
        LocalDateTime scheduledAt,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(NotificationLog log) {
        return new NotificationResponse(
                log.getId(),
                log.getRecipientEmail(),
                log.getSubject(),
                log.getChannel().name(),
                log.getType().name(),
                log.getStatus().name(),
                log.getErrorMessage(),
                log.getSourceEventId(),
                log.getSourceTopic(),
                log.getRetryCount(),
                log.getNextRetryAt(),
                log.getScheduledAt(),
                log.getCreatedAt()
        );
    }
}
