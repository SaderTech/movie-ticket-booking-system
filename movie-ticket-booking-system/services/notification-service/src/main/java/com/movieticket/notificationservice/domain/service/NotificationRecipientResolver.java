package com.movieticket.notificationservice.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.movieticket.notificationservice.infrastructure.client.UserClient;
import com.movieticket.notificationservice.infrastructure.client.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationRecipientResolver {

    private final UserClient userClient;

    public String resolveEmail(JsonNode payload) {
        String email = firstText(payload, "customerEmail", "recipientEmail", "email");
        if (email != null) {
            return email;
        }

        Long userId = longValue(payload, "userId");
        if (userId == null) {
            throw new IllegalArgumentException("Notification event must contain customerEmail/recipientEmail/email or userId");
        }

        try {
            UserResponse user = userClient.getUserById(userId);
            if (user != null && user.email() != null && !user.email().isBlank()) {
                return user.email();
            }
        } catch (Exception e) {
            log.warn("Cannot resolve email for userId={}: {}", userId, e.getMessage());
        }

        throw new IllegalArgumentException("Cannot resolve recipient email for userId=" + userId);
    }

    public String resolveCustomerName(JsonNode payload) {
        String name = firstText(payload, "customerName", "fullName", "username");
        if (name != null) {
            return name;
        }

        Long userId = longValue(payload, "userId");
        if (userId == null) {
            return "khách hàng";
        }

        try {
            UserResponse user = userClient.getUserById(userId);
            if (user != null && user.fullName() != null && !user.fullName().isBlank()) {
                return user.fullName();
            }
            if (user != null && user.username() != null && !user.username().isBlank()) {
                return user.username();
            }
        } catch (Exception e) {
            log.debug("Cannot resolve name for userId={}: {}", userId, e.getMessage());
        }

        return "khách hàng";
    }

    private String firstText(JsonNode payload, String... fields) {
        for (String field : fields) {
            String value = payload.path(field).asText(null);
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
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
