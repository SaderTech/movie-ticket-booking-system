package com.movieticket.notificationservice.infrastructure.scheduler;

import com.movieticket.notificationservice.application.usecase.SendNotificationUseCase;
import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDeliveryScheduler {

    private final NotificationLogRepository notificationLogRepository;
    private final SendNotificationUseCase sendNotificationUseCase;

    @Scheduled(fixedDelayString = "${app.notification.retry.fixed-delay-ms:60000}")
    public void retryFailedNotifications() {
        List<NotificationLog> retryableLogs = notificationLogRepository.findDueRetries();
        if (retryableLogs.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed notifications", retryableLogs.size());
        retryableLogs.forEach(sendNotificationUseCase::sendExisting);
    }

    @Scheduled(fixedDelayString = "${app.notification.reminder.fixed-delay-ms:60000}")
    public void sendScheduledReminders() {
        List<NotificationLog> scheduledLogs = notificationLogRepository.findDueScheduledNotifications();
        if (scheduledLogs.isEmpty()) {
            return;
        }

        log.info("Sending {} scheduled notifications", scheduledLogs.size());
        scheduledLogs.forEach(sendNotificationUseCase::sendExisting);
    }
}
