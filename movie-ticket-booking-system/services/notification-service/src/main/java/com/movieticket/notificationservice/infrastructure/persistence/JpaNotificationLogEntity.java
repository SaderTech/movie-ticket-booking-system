package com.movieticket.notificationservice.infrastructure.persistence;

import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.enums.DeliveryChannel;
import com.movieticket.notificationservice.domain.enums.NotificationStatus;
import com.movieticket.notificationservice.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notification_logs",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_logs_source_event_topic",
                        columnNames = {"source_event_id", "source_topic"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaNotificationLogEntity {

    @Id
    private UUID id;

    @Column(name = "recipient_email", nullable = false, length = 320)
    private String recipientEmail;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeliveryChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationStatus status;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "source_event_id", length = 255)
    private String sourceEventId;

    @Column(name = "source_topic", length = 255)
    private String sourceTopic;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static JpaNotificationLogEntity fromDomain(NotificationLog log) {
        JpaNotificationLogEntity entity = new JpaNotificationLogEntity();
        entity.id = log.getId();
        entity.recipientEmail = log.getRecipientEmail();
        entity.subject = log.getSubject();
        entity.message = log.getMessage();
        entity.channel = log.getChannel();
        entity.type = log.getType();
        entity.status = log.getStatus();
        entity.errorMessage = log.getErrorMessage();
        entity.sourceEventId = log.getSourceEventId();
        entity.sourceTopic = log.getSourceTopic();
        entity.retryCount = log.getRetryCount();
        entity.maxRetries = log.getMaxRetries();
        entity.nextRetryAt = log.getNextRetryAt();
        entity.scheduledAt = log.getScheduledAt();
        entity.createdAt = log.getCreatedAt();
        entity.sentAt = log.getSentAt();
        entity.updatedAt = log.getUpdatedAt();
        return entity;
    }

    public NotificationLog toDomain() {
        return NotificationLog.restore(
                id,
                recipientEmail,
                subject,
                message,
                channel,
                type,
                status,
                errorMessage,
                sourceEventId,
                sourceTopic,
                retryCount,
                maxRetries,
                nextRetryAt,
                scheduledAt,
                createdAt,
                sentAt,
                updatedAt
        );
    }
}
