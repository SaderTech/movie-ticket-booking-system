package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class BookingEventOutbox {
    private Long id;
    private String eventId;
    private String aggregateType;
    private String aggregateId;
    private Long bookingId;
    private String eventType;
    private String topic;
    private String payloadJson;
    private OutboxStatus status;
    private Integer retryCount;
    private String lastError;
    private LocalDateTime createdAt;
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
}
