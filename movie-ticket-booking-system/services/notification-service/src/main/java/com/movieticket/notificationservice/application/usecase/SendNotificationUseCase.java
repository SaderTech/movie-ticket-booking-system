package com.movieticket.notificationservice.application.usecase;

import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.enums.DeliveryChannel;
import com.movieticket.notificationservice.domain.enums.NotificationType;
import com.movieticket.notificationservice.domain.repository.NotificationLogRepository;
import com.movieticket.notificationservice.infrastructure.mail.EmailAttachment;
import com.movieticket.notificationservice.infrastructure.mail.EmailSender;
import com.movieticket.notificationservice.infrastructure.mail.EmailTemplateRenderer;
import com.movieticket.notificationservice.infrastructure.qrcode.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendNotificationUseCase {

    private static final Pattern BOOKING_CODE_PATTERN = Pattern.compile("Mã (booking|đặt vé)\\s*:\\s*([A-Za-z0-9_-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern BOOKING_TEXT_PATTERN = Pattern.compile("booking\\s+([A-Za-z0-9_-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TICKET_CODE_PATTERN = Pattern.compile("\\b(TCK[A-Za-z0-9_-]+)\\b", Pattern.CASE_INSENSITIVE);

    private final NotificationLogRepository notificationLogRepository;
    private final EmailSender emailSender;
    private final EmailTemplateRenderer emailTemplateRenderer;
    private final QrCodeGenerator qrCodeGenerator;

    @Value("${app.notification.max-retries:3}")
    private int maxRetries;

    @Value("${app.public-base-url:http://localhost:8080}")
    private String publicBaseUrl;

    public NotificationLog execute(SendNotificationCommand command) {
        NotificationLog existing = findExistingByIdempotencyKey(command);
        if (existing != null) {
            log.info("Skip duplicated notification event: sourceTopic={}, sourceEventId={}, status={}",
                    command.sourceTopic(), command.sourceEventId(), existing.getStatus());
            return existing;
        }

        NotificationLog logEntry = NotificationLog.create(
                command.recipientEmail(),
                command.subject(),
                command.message(),
                resolveChannel(command.channel()),
                resolveType(command.type()),
                command.sourceEventId(),
                command.sourceTopic(),
                command.scheduledAt(),
                maxRetries
        );

        if (logEntry.isScheduledForFuture()) {
            return notificationLogRepository.save(logEntry);
        }

        deliver(logEntry);
        return notificationLogRepository.save(logEntry);
    }

    public NotificationLog sendExisting(NotificationLog logEntry) {
        deliver(logEntry);
        return notificationLogRepository.save(logEntry);
    }

    private NotificationLog findExistingByIdempotencyKey(SendNotificationCommand command) {
        if (command.sourceEventId() == null || command.sourceEventId().isBlank()
                || command.sourceTopic() == null || command.sourceTopic().isBlank()) {
            return null;
        }

        return notificationLogRepository
                .findBySourceEventIdAndSourceTopic(command.sourceEventId(), command.sourceTopic())
                .orElse(null);
    }

    private void deliver(NotificationLog logEntry) {
        try {
            if (logEntry.getChannel() != DeliveryChannel.EMAIL) {
                throw new IllegalArgumentException("Only EMAIL channel is supported in notification-service MVP");
            }

            List<String> qrTicketCodes = resolveQrTicketCodes(logEntry);
            String qrUrl = qrTicketCodes.isEmpty() ? null : buildTicketQrUrl(qrTicketCodes.get(0));
            List<EmailAttachment> attachments = qrTicketCodes.stream()
                    .map(code -> buildTicketQrAttachment(code, buildTicketQrUrl(code)))
                    .filter(Objects::nonNull)
                    .toList();
            String htmlBody = emailTemplateRenderer.render(logEntry, qrUrl, !attachments.isEmpty());

            emailSender.sendEmail(
                    logEntry.getRecipientEmail(),
                    logEntry.getSubject(),
                    logEntry.getMessage(),
                    htmlBody,
                    attachments
            );

            logEntry.markSent();
        } catch (Exception e) {
            String error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            logEntry.markDeliveryFailure(error);
        }
    }

    private List<String> resolveQrTicketCodes(NotificationLog logEntry) {
        if (logEntry.getType() != NotificationType.TICKET_BOOKED) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> ticketCodes = new LinkedHashSet<>();
        ticketCodes.addAll(extractTicketCodes(logEntry.getMessage()));
        ticketCodes.addAll(extractTicketCodes(logEntry.getSubject()));
        return new ArrayList<>(ticketCodes);
    }

    private List<String> extractTicketCodes(String value) {
        if (value == null || value.isBlank()) {
            return Collections.emptyList();
        }

        LinkedHashSet<String> ticketCodes = new LinkedHashSet<>();
        Matcher ticketCodeMatcher = TICKET_CODE_PATTERN.matcher(value);
        while (ticketCodeMatcher.find()) {
            ticketCodes.add(ticketCodeMatcher.group(1).trim());
        }
        return new ArrayList<>(ticketCodes);
    }

    private Optional<String> extractBookingCode(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }

        Matcher bookingCodeMatcher = BOOKING_CODE_PATTERN.matcher(value);
        if (bookingCodeMatcher.find()) {
            return Optional.of(bookingCodeMatcher.group(2).trim());
        }

        Matcher bookingTextMatcher = BOOKING_TEXT_PATTERN.matcher(value);
        if (bookingTextMatcher.find()) {
            return Optional.of(bookingTextMatcher.group(1).trim());
        }

        int dashIndex = value.lastIndexOf('-');
        if (dashIndex >= 0 && dashIndex < value.length() - 1) {
            String suffix = value.substring(dashIndex + 1).trim();
            if (suffix.matches("[A-Za-z0-9_-]+")) {
                return Optional.of(suffix);
            }
        }

        return Optional.empty();
    }

    private EmailAttachment buildTicketQrAttachment(String ticketCode, String qrUrl) {
        try {
            byte[] content = qrCodeGenerator.generate(qrUrl != null ? qrUrl : ticketCode);
            return new EmailAttachment("ticket-qr-" + ticketCode + ".png", content, "image/png");
        } catch (Exception e) {
            log.warn("Could not generate QR attachment for ticketCode={}: {}", ticketCode, e.getMessage());
            return null;
        }
    }

    private String buildTicketQrUrl(String ticketCode) {
        String baseUrl = publicBaseUrl == null || publicBaseUrl.isBlank()
                ? "http://localhost:8080"
                : publicBaseUrl.trim();
        while (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/api/v1/qr-codes/ticket/" + URLEncoder.encode(ticketCode, StandardCharsets.UTF_8);
    }

    private DeliveryChannel resolveChannel(String channel) {
        if (channel == null || channel.isBlank()) {
            return DeliveryChannel.EMAIL;
        }

        return DeliveryChannel.valueOf(channel.toUpperCase());
    }

    private NotificationType resolveType(String type) {
        if (type == null || type.isBlank()) {
            return NotificationType.SYSTEM_ALERT;
        }

        return NotificationType.valueOf(type.toUpperCase());
    }
}
