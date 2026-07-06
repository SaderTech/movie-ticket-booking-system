package com.movieticket.notificationservice.domain.repository;

import com.movieticket.notificationservice.domain.entity.NotificationLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationLogRepository {

    NotificationLog save(NotificationLog log);

    Optional<NotificationLog> findById(UUID id);

    Optional<NotificationLog> findBySourceEventIdAndSourceTopic(String sourceEventId, String sourceTopic);

    List<NotificationLog> findAll();

    List<NotificationLog> findDueRetries();

    List<NotificationLog> findDueScheduledNotifications();
}
