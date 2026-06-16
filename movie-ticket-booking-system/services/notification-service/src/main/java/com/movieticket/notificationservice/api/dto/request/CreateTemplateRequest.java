package com.movieticket.notificationservice.api.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTemplateRequest(
        @NotBlank(message = "Template code is required")
        String code,

        @NotBlank(message = "Template subject is required")
        String subject,

        @NotBlank(message = "Template body is required")
        String body
) {
}