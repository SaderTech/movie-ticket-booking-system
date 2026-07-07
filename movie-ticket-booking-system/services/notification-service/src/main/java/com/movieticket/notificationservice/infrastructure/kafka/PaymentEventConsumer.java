package com.movieticket.notificationservice.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.domain.service.NotificationRecipientResolver;
import com.movieticket.notificationservice.application.usecase.SendNotificationUseCase;
import com.movieticket.notificationservice.config.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final NotificationRecipientResolver recipientResolver;

    @KafkaListener(
            topics = {Constants.TOPIC_PAYMENT_SUCCESS, Constants.TOPIC_PAYMENT_FAILED},
            groupId = "${spring.kafka.consumer.group-id:notification-service}"
    )
    public void consumePaymentEvent(String message, ConsumerRecord<String, String> record) {
        String topic = record.topic();
        try {
            JsonNode payload = objectMapper.readTree(message);
            String recipientEmail = recipientResolver.resolveEmail(payload);
            String paymentCode = text(payload, "paymentCode", text(payload, "transactionRef", "unknown-payment"));
            String amount = text(payload, "amount", "0");
            String sourceEventId = sourceEventId(payload, message, paymentCode, topic);

            if (Constants.TOPIC_PAYMENT_SUCCESS.equals(topic)) {
                sendNotificationUseCase.execute(new SendNotificationCommand(
                        recipientEmail,
                        "Thanh toán thành công - " + paymentCode,
                        "Thanh toán " + paymentCode + " với số tiền " + amount + " đã hoàn tất thành công.",
                        "EMAIL",
                        "PAYMENT_SUCCESS",
                        sourceEventId,
                        topic,
                        null
                ));
            } else {
                sendNotificationUseCase.execute(new SendNotificationCommand(
                        recipientEmail,
                        "Thanh toán thất bại - " + paymentCode,
                        "Thanh toán " + paymentCode + " với số tiền " + amount + " chưa thành công. Vui lòng thử lại.",
                        "EMAIL",
                        "PAYMENT_FAILED",
                        sourceEventId,
                        topic,
                        null
                ));
            }
        } catch (Exception e) {
            log.error("Failed to consume payment event from topic={}: {}", topic, e.getMessage(), e);
        }
    }

    private String text(JsonNode payload, String field, String defaultValue) {
        String value = payload.path(field).asText(null);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private String sourceEventId(JsonNode payload, String rawMessage, String aggregateValue, String suffix) {
        String eventId = payload.path("eventId").asText(null);
        if (eventId != null && !eventId.isBlank()) {
            return eventId.trim();
        }
        if (aggregateValue != null && !aggregateValue.isBlank()) {
            return aggregateValue.trim() + ":" + suffix;
        }
        return sha256(rawMessage) + ":" + suffix;
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            return String.valueOf(value.hashCode());
        }
    }
}
