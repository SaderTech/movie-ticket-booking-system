package com.movieticket.notificationservice.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.movieticket.notificationservice.infrastructure.client.ShowtimeClient;
import com.movieticket.notificationservice.infrastructure.client.dto.ShowtimeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowtimeReminderPlanner {

    private final ShowtimeClient showtimeClient;

    @Value("${app.notification.reminder.minutes-before:60}")
    private long reminderMinutesBefore;

    public Optional<LocalDateTime> resolveReminderAt(JsonNode payload) {
        Optional<LocalDateTime> showtimeStart = resolveShowtimeStart(payload);
        return showtimeStart.map(startAt -> startAt.minusMinutes(reminderMinutesBefore));
    }

    public String buildShowtimeLabel(JsonNode payload) {
        Optional<LocalDateTime> showtimeStart = resolveShowtimeStart(payload);
        return showtimeStart
                .map(value -> value.toLocalDate() + " lúc " + value.toLocalTime())
                .orElse("suất chiếu của bạn");
    }

    private Optional<LocalDateTime> resolveShowtimeStart(JsonNode payload) {
        Optional<LocalDateTime> direct = fromDirectFields(payload);
        if (direct.isPresent()) {
            return direct;
        }

        Long showtimeId = longValue(payload, "showtimeId");
        if (showtimeId == null) {
            return Optional.empty();
        }

        try {
            ShowtimeResponse showtime = showtimeClient.getShowtimeById(showtimeId);
            if (showtime != null && showtime.showDate() != null && showtime.startTime() != null) {
                return Optional.of(LocalDateTime.of(showtime.showDate(), showtime.startTime()));
            }
        } catch (Exception e) {
            log.warn("Cannot resolve showtime detail for showtimeId={}: {}", showtimeId, e.getMessage());
        }

        return Optional.empty();
    }

    private Optional<LocalDateTime> fromDirectFields(JsonNode payload) {
        String showtimeStartAt = text(payload, "showtimeStartAt");
        if (showtimeStartAt != null) {
            try {
                return Optional.of(LocalDateTime.parse(showtimeStartAt));
            } catch (DateTimeParseException ignored) {
                // Try showDate/startTime below.
            }
        }

        String showDate = text(payload, "showDate");
        String startTime = text(payload, "startTime");
        if (showDate == null || startTime == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(LocalDateTime.of(LocalDate.parse(showDate), LocalTime.parse(startTime)));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private String text(JsonNode payload, String field) {
        String value = payload.path(field).asText(null);
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Long longValue(JsonNode payload, String field) {
        JsonNode node = payload.path(field);
        if (node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            return node.asLong();
        }
        String text = node.asText(null);
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
