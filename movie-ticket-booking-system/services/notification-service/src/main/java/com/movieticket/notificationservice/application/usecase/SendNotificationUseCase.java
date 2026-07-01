package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.domain.model.DeliveryChannel;
import com.movieticket.notificationservice.domain.model.NotificationLog;
import com.movieticket.notificationservice.domain.model.NotificationType;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import com.movieticket.notificationservice.infrastructure.mail.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendNotificationUseCase {

    private final NotificationLogRepository notificationLogRepository;
    private final EmailSender emailSender;

    public NotificationLog execute(SendNotificationCommand command) {
        NotificationLog log = NotificationLog.create(
                command.recipientEmail(),
                command.subject(),
                command.message(),
                resolveChannel(command.channel()),
                resolveType(command.type())
        );

        try {
            emailSender.sendEmail(
                    command.recipientEmail(),
                    command.subject(),
                    command.message()
            );

            log.markSent();
        } catch (Exception e) {
            log.markFailed(e.getMessage());
        }

        return notificationLogRepository.save(log);
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