package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.domain.enums.DeliveryChannel;
import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.enums.NotificationType;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import com.movieticket.notificationservice.infrastructure.mail.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendNotificationUseCase {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailSender emailSender;

    @Value("${app.notification.max-retries:3}")
    private int maxRetries;

    public NotificationLog execute(SendNotificationCommand command) {
        NotificationLog existing = findExistingByIdempotencyKey(command);
        if (existing != null) {
            log.info("Skip duplicated notification event: sourceTopic={}, sourceEventId={}, status={}",
                    command.sourceTopic(), command.sourceEventId(), existing.getStatus());
            return existing;
        }

        NotificationLog logEntry = NotificationLog.create(
                command.recipientEmail(),
                command.subject(),
                command.message(),
                resolveChannel(command.channel()),
                resolveType(command.type()),
                command.sourceEventId(),
                command.sourceTopic(),
                command.scheduledAt(),
                maxRetries
        );

        if (logEntry.isScheduledForFuture()) {
            return notificationLogRepository.save(logEntry);
        }

        deliver(logEntry);
        return notificationLogRepository.save(logEntry);
    }

    public NotificationLog sendExisting(NotificationLog logEntry) {
        deliver(logEntry);
        return notificationLogRepository.save(logEntry);
    }

    private NotificationLog findExistingByIdempotencyKey(SendNotificationCommand command) {
        if (command.sourceEventId() == null || command.sourceEventId().isBlank()
                || command.sourceTopic() == null || command.sourceTopic().isBlank()) {
            return null;
        }

        return notificationLogRepository
                .findBySourceEventIdAndSourceTopic(command.sourceEventId(), command.sourceTopic())
                .orElse(null);
    }

    private void deliver(NotificationLog logEntry) {
        try {
            if (logEntry.getChannel() != DeliveryChannel.EMAIL) {
                throw new IllegalArgumentException("Only EMAIL channel is supported in notification-service MVP");
            }

            emailSender.sendEmail(
                    logEntry.getRecipientEmail(),
                    logEntry.getSubject(),
                    logEntry.getMessage()
            );

            logEntry.markSent();
        } catch (Exception e) {
            String error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            logEntry.markDeliveryFailure(error);
        }
    }

    private DeliveryChannel resolveChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return DeliveryChannel.EMAIL;
        }

        return DeliveryChannel.valueOf(channel.toUpperCase());
    }

    private NotificationType resolveType(String type) {
        if (type == null || type.isBlank()) {
            return NotificationType.SYSTEM_ALERT;
        }

        return NotificationType.valueOf(type.toUpperCase());
    }
}
