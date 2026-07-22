package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.api.exception.NotificationException;
import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetNotificationLogUseCase {

    private final NotificationLogRepository notificationLogRepository;

    public NotificationLog execute(UUID id) {
        return notificationLogRepository.findById(id)
                .orElseThrow(() -> new NotificationException("Notification log not found: " + id));
    }
}
