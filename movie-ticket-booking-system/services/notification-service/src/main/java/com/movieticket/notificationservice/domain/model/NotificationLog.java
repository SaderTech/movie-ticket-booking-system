package com.movieticket.notificationservice.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private LocalDateTime createdAt;

    public static NotificationLog create(
            String recipientEmail,
            String subject,
            String message,
            DeliveryChannel channel,
            NotificationType type
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

        NotificationLog log = new NotificationLog();
        log.id = UUID.randomUUID();
        log.recipientEmail = recipientEmail;
        log.subject = subject;
        log.message = message;
        log.channel = channel == null ? DeliveryChannel.EMAIL : channel;
        log.type = type == null ? NotificationType.SYSTEM_ALERT : type;
        log.status = NotificationStatus.PENDING;
        log.createdAt = LocalDateTime.now();

        return log;
    }

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}