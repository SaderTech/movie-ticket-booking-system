package com.movieticket.notificationservice.domain.repository;

import com.movieticket.notificationservice.domain.model.NotificationLog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationLogRepository {

    NotificationLog save(NotificationLog log);

    Optional<NotificationLog> findById(UUID id);

    List<NotificationLog> findAll();
}