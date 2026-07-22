package com.movieticket.notificationservice.api.controller;

import com.movieticket.notificationservice.api.dto.response.NotificationLogResponse;
import com.movieticket.notificationservice.api.dto.response.NotificationResponse;
import com.movieticket.notificationservice.api.dto.response.PageResponse;
import com.movieticket.notificationservice.api.dto.request.SendNotificationRequest;
import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.application.mapper.NotificationMapper;
import com.movieticket.notificationservice.application.usecase.GetNotificationLogUseCase;
import com.movieticket.notificationservice.application.usecase.ListNotificationLogsUseCase;
import com.movieticket.notificationservice.application.usecase.ResendNotificationUseCase;
import com.movieticket.notificationservice.application.usecase.SearchNotificationLogsUseCase;
import com.movieticket.notificationservice.application.usecase.SendNotificationUseCase;
import com.movieticket.notificationservice.domain.entity.NotificationLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiPath.NOTIFICATIONS)
@RequiredArgsConstructor
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;
    private final ListNotificationLogsUseCase listNotificationLogsUseCase;
    private final GetNotificationLogUseCase getNotificationLogUseCase;
    private final SearchNotificationLogsUseCase searchNotificationLogsUseCase;
    private final ResendNotificationUseCase resendNotificationUseCase;
    private final NotificationMapper notificationMapper;

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

        return notificationMapper.toResponse(log);
    }

    @GetMapping(ApiPath.LOGS)
    public List<NotificationLogResponse> logs() {
        return listNotificationLogsUseCase.execute()
                .stream()
                .map(notificationMapper::toLogResponse)
                .toList();
    }

    @GetMapping(ApiPath.LOG_SEARCH)
    public PageResponse<NotificationLogResponse> searchLogs(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return searchNotificationLogsUseCase.execute(status, type, email, page, size);
    }

    @GetMapping(ApiPath.DETAIL)
    public NotificationLogResponse detail(@PathVariable("id") UUID id) {
        return notificationMapper.toLogResponse(getNotificationLogUseCase.execute(id));
    }

    @PostMapping(ApiPath.RESEND)
    public NotificationResponse resend(@PathVariable("id") UUID id) {
        return notificationMapper.toResponse(resendNotificationUseCase.execute(id));
    }
}
