package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListNotificationLogsUseCase {

    private final NotificationLogRepository notificationLogRepository;

    public List<NotificationLog> execute() {
        return notificationLogRepository.findAll();
    }
}