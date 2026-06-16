package com.movieticket.notificationservice.infrastructure.persistence;

import com.movieticket.notificationservice.domain.model.NotificationLog;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryNotificationLogRepositoryAdapter implements NotificationLogRepository {

    private final ConcurrentHashMap<UUID, NotificationLog> storage = new ConcurrentHashMap<>();

    @Override
    public NotificationLog save(NotificationLog log) {
        storage.put(log.getId(), log);
        return log;
    }

    @Override
    public Optional<NotificationLog> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<NotificationLog> findAll() {
        return storage.values()
                .stream()
                .sorted(Comparator.comparing(NotificationLog::getCreatedAt).reversed())
                .toList();
    }
}