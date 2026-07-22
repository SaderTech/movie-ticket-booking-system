package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.api.dto.response.NotificationLogResponse;
import com.movieticket.notificationservice.api.dto.response.PageResponse;
import com.movieticket.notificationservice.application.mapper.NotificationMapper;
import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.enums.NotificationStatus;
import com.movieticket.notificationservice.domain.enums.NotificationType;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class SearchNotificationLogsUseCase {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationMapper notificationMapper;

    public PageResponse<NotificationLogResponse> execute(
            String status,
            String type,
            String recipientEmail,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);

        NotificationStatus statusFilter = parseStatus(status);
        NotificationType typeFilter = parseType(type);
        String emailFilter = normalize(recipientEmail);

        List<NotificationLogResponse> filtered = notificationLogRepository.findAll()
                .stream()
                .filter(log -> statusFilter == null || log.getStatus() == statusFilter)
                .filter(log -> typeFilter == null || log.getType() == typeFilter)
                .filter(log -> emailFilter == null || containsIgnoreCase(log.getRecipientEmail(), emailFilter))
                .map(notificationMapper::toLogResponse)
                .toList();

        int fromIndex = Math.min(safePage * safeSize, filtered.size());
        int toIndex = Math.min(fromIndex + safeSize, filtered.size());
        int totalPages = filtered.isEmpty() ? 0 : (int) Math.ceil((double) filtered.size() / safeSize);

        return new PageResponse<>(
                filtered.subList(fromIndex, toIndex),
                safePage,
                safeSize,
                filtered.size(),
                totalPages
        );
    }

    private NotificationStatus parseStatus(String status) {
        String value = normalize(status);
        return value == null ? null : NotificationStatus.valueOf(value.toUpperCase(Locale.ROOT));
    }

    private NotificationType parseType(String type) {
        String value = normalize(type);
        return value == null ? null : NotificationType.valueOf(value.toUpperCase(Locale.ROOT));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (source == null) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }
}
