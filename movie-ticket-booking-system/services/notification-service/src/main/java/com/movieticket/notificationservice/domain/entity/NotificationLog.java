package com.movieticket.notificationservice.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import com.movieticket.notificationservice.domain.enums.DeliveryChannel;
import com.movieticket.notificationservice.domain.enums.NotificationStatus;
import com.movieticket.notificationservice.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationLog {

    private UUID id;
    private String recipientEmail;
    private String subject;
    private String message;
    private DeliveryChannel channel;
    private NotificationType type;
    private NotificationStatus status;
    private String errorMessage;
    private String sourceEventId;
    private String sourceTopic;
    private int retryCount;
    private int maxRetries;
    private LocalDateTime nextRetryAt;
    private LocalDateTime scheduledAt;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime updatedAt;

    public static NotificationLog create(
            String recipientEmail,
            String subject,
            String message,
            DeliveryChannel channel,
            NotificationType type,
            String sourceEventId,
            String sourceTopic,
            LocalDateTime scheduledAt,
            int maxRetries
    ) {
        if (recipientEmail == null || recipientEmail.isBlank()) {
            throw new IllegalArgumentException("Recipient email is required");
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Subject is required");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message is required");
        }

        LocalDateTime now = LocalDateTime.now();

        NotificationLog log = new NotificationLog();
        log.id = UUID.randomUUID();
        log.recipientEmail = recipientEmail.trim();
        log.subject = subject.trim();
        log.message = message;
        log.channel = channel == null ? DeliveryChannel.EMAIL : channel;
        log.type = type == null ? NotificationType.SYSTEM_ALERT : type;
        log.status = NotificationStatus.PENDING;
        log.sourceEventId = normalize(sourceEventId);
        log.sourceTopic = normalize(sourceTopic);
        log.retryCount = 0;
        log.maxRetries = Math.max(maxRetries, 0);
        log.scheduledAt = scheduledAt;
        log.createdAt = now;
        log.updatedAt = now;

        return log;
    }

    public static NotificationLog create(
            String recipientEmail,
            String subject,
            String message,
            DeliveryChannel channel,
            NotificationType type
    ) {
        return create(recipientEmail, subject, message, channel, type, null, null, null, 3);
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.errorMessage = null;
        this.sentAt = LocalDateTime.now();
        this.nextRetryAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.nextRetryAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public void markDeliveryFailure(String errorMessage) {
        this.retryCount++;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();

        if (this.retryCount >= this.maxRetries) {
            this.status = NotificationStatus.FAILED;
            this.nextRetryAt = null;
            return;
        }

        this.status = NotificationStatus.RETRYING;
        this.nextRetryAt = LocalDateTime.now().plusMinutes(calculateBackoffMinutes());
    }

    public boolean isScheduledForFuture() {
        return scheduledAt != null && scheduledAt.isAfter(LocalDateTime.now());
    }

    public boolean isDueToSend() {
        return status == NotificationStatus.PENDING
                && scheduledAt != null
                && !scheduledAt.isAfter(LocalDateTime.now());
    }

    public boolean isDueToRetry() {
        return status == NotificationStatus.RETRYING
                && retryCount < maxRetries
                && nextRetryAt != null
                && !nextRetryAt.isAfter(LocalDateTime.now());
    }

    public boolean hasIdempotencyKey() {
        return sourceEventId != null && !sourceEventId.isBlank()
                && sourceTopic != null && !sourceTopic.isBlank();
    }

    private int calculateBackoffMinutes() {
        return Math.min(30, (int) Math.pow(2, Math.max(0, retryCount - 1)));
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
