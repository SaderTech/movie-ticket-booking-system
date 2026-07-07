package com.movieticket.notificationservice.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationEvent {

    private UUID id;
    private String sourceService;
    private String eventType;
    private String payload;
    private LocalDateTime receivedAt;

    public static NotificationEvent create(String sourceService, String eventType, String payload) {
        NotificationEvent event = new NotificationEvent();
        event.id = UUID.randomUUID();
        event.sourceService = sourceService;
        event.eventType = eventType;
        event.payload = payload;
        event.receivedAt = LocalDateTime.now();

        return event;
    }
}