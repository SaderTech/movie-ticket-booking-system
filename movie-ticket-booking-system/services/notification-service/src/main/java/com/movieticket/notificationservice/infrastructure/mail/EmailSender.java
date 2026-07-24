package com.movieticket.notificationservice.infrastructure.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EmailSender {

    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    public void sendEmail(String to, String subject, String body) {
        sendEmail(to, subject, body, null, List.of());
    }

    public void sendEmail(String to, String subject, String body, String htmlBody, List<EmailAttachment> attachments) {
        if (to == null || to.isBlank()) {
            throw new IllegalArgumentException("Recipient email must not be empty");
        }

        try {
            List<EmailAttachment> safeAttachments = attachments == null ? List.of() : attachments;
            boolean hasHtml = htmlBody != null && !htmlBody.isBlank();

// Cần multipart nếu có HTML alternative hoặc có attachment QR
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setFrom(resolveFromEmail());
            helper.setTo(to.trim());
            helper.setSubject(subject);
            if (hasHtml) {
                helper.setText(body, htmlBody);
            } else {
                helper.setText(body, false);
            }

            for (EmailAttachment attachment : safeAttachments) {
                if (attachment == null || attachment.content() == null || attachment.content().length == 0) {
                    continue;
                }
                helper.addAttachment(
                        attachment.filename(),
                        new ByteArrayResource(attachment.content()),
                        attachment.contentType()
                );
            }

            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            String error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            throw new IllegalStateException(error, e);
        }
    }

    private String resolveFromEmail() {
        if (fromEmail != null && !fromEmail.isBlank()) {
            return fromEmail;
        }

        return mailProperties.getUsername();
    }
}
