package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking_event_outbox",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_booking_event_outbox_event_id", columnNames = {"event_id"})
       },
       indexes = {
            @Index(name = "idx_booking_event_outbox_status_created", columnList = "status, created_at")
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingEventOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "aggregate_type", length = 50)
    private String aggregateType;

    @Column(name = "aggregate_id", length = 100)
    private String aggregateId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "topic", nullable = false, length = 100)
    private String topic;

    @Column(name = "payload_json", nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public void markPublished() {
        status = OutboxStatus.PUBLISHED;
        publishedAt = LocalDateTime.now();
    }

    public void markFailed(String error) {
        status = OutboxStatus.FAILED;
        lastError = error;
    }

    public void incrementRetry() {
        if (retryCount == null) {
            retryCount = 1;
        } else {
            retryCount++;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (retryCount == null) {
            retryCount = 0;
        }
    }
}