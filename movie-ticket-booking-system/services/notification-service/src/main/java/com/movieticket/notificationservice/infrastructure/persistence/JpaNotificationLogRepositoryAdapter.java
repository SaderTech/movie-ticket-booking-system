package com.movieticket.notificationservice.infrastructure.persistence;

import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.enums.NotificationStatus;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@Primary
@RequiredArgsConstructor
public class JpaNotificationLogRepositoryAdapter implements NotificationLogRepository {

    private final SpringDataNotificationLogJpaRepository repository;

    @Override
    public NotificationLog save(NotificationLog log) {
        return repository.save(JpaNotificationLogEntity.fromDomain(log)).toDomain();
    }

    @Override
    public Optional<NotificationLog> findById(UUID id) {
        return repository.findById(id).map(JpaNotificationLogEntity::toDomain);
    }

    @Override
    public Optional<NotificationLog> findBySourceEventIdAndSourceTopic(String sourceEventId, String sourceTopic) {
        if (sourceEventId == null || sourceEventId.isBlank() || sourceTopic == null || sourceTopic.isBlank()) {
            return Optional.empty();
        }

        return repository.findBySourceEventIdAndSourceTopic(sourceEventId, sourceTopic)
                .map(JpaNotificationLogEntity::toDomain);
    }

    @Override
    public List<NotificationLog> findAll() {
        return repository.findAll()
                .stream()
                .map(JpaNotificationLogEntity::toDomain)
                .sorted(Comparator.comparing(NotificationLog::getCreatedAt).reversed())
                .toList();
    }

    @Override
    public List<NotificationLog> findDueRetries() {
        return repository.findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
                        NotificationStatus.RETRYING,
                        LocalDateTime.now()
                )
                .stream()
                .map(JpaNotificationLogEntity::toDomain)
                .toList();
    }

    @Override
    public List<NotificationLog> findDueScheduledNotifications() {
        return repository.findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
                        NotificationStatus.PENDING,
                        LocalDateTime.now()
                )
                .stream()
                .map(JpaNotificationLogEntity::toDomain)
                .toList();
    }
}
