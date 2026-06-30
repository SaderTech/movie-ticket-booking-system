package com.movieticket.notificationservice.api.controller;

import com.movieticket.notificationservice.api.dto.response.NotificationLogResponse;
import com.movieticket.notificationservice.api.dto.response.NotificationResponse;
import com.movieticket.notificationservice.api.dto.request.SendNotificationRequest;
import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.application.usecase.ListNotificationLogsUseCase;
import com.movieticket.notificationservice.application.usecase.SendNotificationUseCase;
import com.movieticket.notificationservice.domain.model.NotificationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiPath.NOTIFICATIONS)
@RequiredArgsConstructor
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ListNotificationLogsUseCase listNotificationLogsUseCase;

    @PostMapping(ApiPath.SEND)
    public NotificationResponse send(@Valid @RequestBody SendNotificationRequest request) {
        NotificationLog log = sendNotificationUseCase.execute(
                new SendNotificationCommand(
                        request.recipientEmail(),
                        request.subject(),
                        request.message(),
                        request.channel(),
                        request.type()
                )
        );

        return NotificationResponse.from(log);
    }

    @GetMapping(ApiPath.LOGS)
    public List<NotificationLogResponse> logs() {
        return listNotificationLogsUseCase.execute()
                .stream()
                .map(NotificationLogResponse::from)
                .toList();
    }
}