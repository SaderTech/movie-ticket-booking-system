package com.movieticket.notificationservice.infrastructure.persistence;

import com.movieticket.notificationservice.domain.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataNotificationLogJpaRepository extends JpaRepository<JpaNotificationLogEntity, UUID> {

    Optional<JpaNotificationLogEntity> findBySourceEventIdAndSourceTopic(String sourceEventId, String sourceTopic);

    List<JpaNotificationLogEntity> findByStatusAndNextRetryAtLessThanEqualOrderByNextRetryAtAsc(
            NotificationStatus status,
            LocalDateTime now
    );

    List<JpaNotificationLogEntity> findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
            NotificationStatus status,
            LocalDateTime now
    );
}
