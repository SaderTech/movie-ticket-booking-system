package com.movieticket.notificationservice.application.mapper;

import com.movieticket.notificationservice.api.dto.response.NotificationLogResponse;
import com.movieticket.notificationservice.api.dto.response.NotificationResponse;
import com.movieticket.notificationservice.domain.entity.NotificationLog;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(NotificationLog log) {
        return NotificationResponse.from(log);
    }

    public NotificationLogResponse toLogResponse(NotificationLog log) {
        return NotificationLogResponse.from(log);
    }
}
