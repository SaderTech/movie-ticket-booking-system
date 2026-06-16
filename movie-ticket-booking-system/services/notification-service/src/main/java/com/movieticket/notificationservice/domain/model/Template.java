package com.movieticket.notificationservice.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Template {

    private UUID id;
    private String code;
    private String subject;
    private String body;

    public static Template create(String code, String subject, String body) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Template code is required");
        }

        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("Template subject is required");
        }

        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Template body is required");
        }

        Template template = new Template();
        template.id = UUID.randomUUID();
        template.code = code.trim();
        template.subject = subject;
        template.body = body;

        return template;
    }
}