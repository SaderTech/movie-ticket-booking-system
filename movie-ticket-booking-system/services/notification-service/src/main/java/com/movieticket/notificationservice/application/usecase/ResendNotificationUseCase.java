package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.api.exception.NotificationException;
import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.enums.NotificationStatus;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResendNotificationUseCase {

    private final NotificationLogRepository notificationLogRepository;
    private final SendNotificationUseCase sendNotificationUseCase;

    public NotificationLog execute(UUID id) {
        NotificationLog log = notificationLogRepository.findById(id)
                .orElseThrow(() -> new NotificationException("Notification log not found: " + id));

        if (log.getStatus() == NotificationStatus.SENT) {
            return log;
        }

        return sendNotificationUseCase.sendExisting(log);
    }
}
