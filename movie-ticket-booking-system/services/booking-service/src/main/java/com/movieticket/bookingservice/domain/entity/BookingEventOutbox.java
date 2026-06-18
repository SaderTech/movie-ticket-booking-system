package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
}
