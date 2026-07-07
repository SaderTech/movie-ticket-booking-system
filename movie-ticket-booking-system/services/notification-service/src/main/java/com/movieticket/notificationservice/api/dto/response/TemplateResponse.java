package com.movieticket.notificationservice.api.dto.response;

import com.movieticket.notificationservice.domain.entity.Template;

import java.util.UUID;

public record TemplateResponse(
        UUID id,
        String code,
        String subject,
        String body
) {
    public static TemplateResponse from(Template template) {
        return new TemplateResponse(
                template.getId(),
                template.getCode(),
                template.getSubject(),
                template.getBody()
        );
    }
}