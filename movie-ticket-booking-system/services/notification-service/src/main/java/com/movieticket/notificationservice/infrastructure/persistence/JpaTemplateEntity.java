package com.movieticket.notificationservice.infrastructure.persistence;

import com.movieticket.notificationservice.domain.entity.Template;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "notification_templates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JpaTemplateEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 500)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    public static JpaTemplateEntity fromDomain(Template template) {
        JpaTemplateEntity entity = new JpaTemplateEntity();
        entity.id = template.getId();
        entity.code = template.getCode();
        entity.subject = template.getSubject();
        entity.body = template.getBody();
        return entity;
    }

    public Template toDomain() {
        return Template.restore(id, code, subject, body);
    }
}
