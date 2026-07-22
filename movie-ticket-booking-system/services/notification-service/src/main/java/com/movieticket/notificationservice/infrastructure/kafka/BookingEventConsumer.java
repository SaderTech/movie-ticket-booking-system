package com.movieticket.notificationservice.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.domain.service.NotificationRecipientResolver;
import com.movieticket.notificationservice.domain.service.ShowtimeReminderPlanner;
import com.movieticket.notificationservice.application.usecase.SendNotificationUseCase;
import com.movieticket.notificationservice.config.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventConsumer {

    private final ObjectMapper objectMapper;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final NotificationRecipientResolver recipientResolver;
    private final ShowtimeReminderPlanner showtimeReminderPlanner;

    @KafkaListener(
            topics = {
                    Constants.TOPIC_BOOKING_CONFIRMED,
                    Constants.TOPIC_BOOKING_CANCELLED,
                    Constants.TOPIC_TICKET_BOOKED,
                    Constants.TOPIC_SEAT_HOLD_CREATED,
                    Constants.TOPIC_SEAT_HOLD_EXPIRED
            },
            groupId = "${spring.kafka.consumer.group-id:notification-service}"
    )
    public void consumeBookingEvent(String message, org.apache.kafka.clients.consumer.ConsumerRecord<String, String> record) {
        String topic = record.topic();
        try {
            JsonNode payload = objectMapper.readTree(message);
            switch (topic) {
                case Constants.TOPIC_BOOKING_CONFIRMED -> handleBookingConfirmed(payload, topic, message);
                case Constants.TOPIC_BOOKING_CANCELLED -> handleBookingCancelled(payload, topic, message);
                case Constants.TOPIC_TICKET_BOOKED -> handleTicketBooked(payload, topic, message);
                case Constants.TOPIC_SEAT_HOLD_CREATED -> handleSeatHoldCreated(payload, topic, message);
                case Constants.TOPIC_SEAT_HOLD_EXPIRED -> handleSeatHoldExpired(payload, topic, message);
                default -> log.debug("Ignored booking event from topic={}", topic);
            }
        } catch (Exception e) {
            log.error("Failed to consume booking event from topic={}: {}", topic, e.getMessage(), e);
        }
    }

    private void handleBookingConfirmed(JsonNode payload, String topic, String rawMessage) {
        String recipientEmail = recipientResolver.resolveEmail(payload);
        String customerName = recipientResolver.resolveCustomerName(payload);
        String bookingCode = text(payload, "bookingCode", "unknown-booking");
        String totalAmount = text(payload, "totalAmount", "0");
        String movieTitle = text(payload, "movieTitle", "bộ phim bạn đã chọn");
        String showtimeLabel = showtimeReminderPlanner.buildShowtimeLabel(payload);
        String qrEndpoint = "/api/v1/qr-codes/ticket/" + bookingCode;
        String sourceEventId = sourceEventId(payload, rawMessage, bookingCode, "booking-confirmed");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Bạn đã đặt vé thành công.\n"
                + "Mã booking: " + bookingCode + "\n"
                + "Phim: " + movieTitle + "\n"
                + "Suất chiếu: " + showtimeLabel + "\n"
                + "Tổng tiền: " + totalAmount + "\n"
                + "QR vé: " + qrEndpoint + "\n\n"
                + "Vui lòng đưa mã QR này cho nhân viên rạp khi check-in.";

        sendNotificationUseCase.execute(new SendNotificationCommand(
                recipientEmail,
                "Xác nhận đặt vé thành công - " + bookingCode,
                message,
                "EMAIL",
                "BOOKING_CONFIRMATION",
                sourceEventId,
                topic,
                null
        ));

        scheduleShowtimeReminder(payload, recipientEmail, customerName, bookingCode, movieTitle, showtimeLabel, sourceEventId);
    }

    private void handleBookingCancelled(JsonNode payload, String topic, String rawMessage) {
        String recipientEmail = recipientResolver.resolveEmail(payload);
        String customerName = recipientResolver.resolveCustomerName(payload);
        String bookingCode = text(payload, "bookingCode", "unknown-booking");
        String reason = text(payload, "reason", "Không có lý do cụ thể");
        String sourceEventId = sourceEventId(payload, rawMessage, bookingCode, "booking-cancelled");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Booking " + bookingCode + " của bạn đã được hủy.\n"
                + "Lý do: " + reason + "\n\n"
                + "Nếu booking đã thanh toán, hệ thống sẽ xử lý hoàn tiền theo chính sách của rạp.";

        sendNotificationUseCase.execute(new SendNotificationCommand(
                recipientEmail,
                "Thông báo hủy vé - " + bookingCode,
                message,
                "EMAIL",
                "BOOKING_CANCELLED",
                sourceEventId,
                topic,
                null
        ));
    }

    private void handleTicketBooked(JsonNode payload, String topic, String rawMessage) {
        String recipientEmail = recipientResolver.resolveEmail(payload);
        String customerName = recipientResolver.resolveCustomerName(payload);
        String bookingCode = text(payload, "bookingCode", "unknown-booking");
        String sourceEventId = sourceEventId(payload, rawMessage, bookingCode, "ticket-booked");

        String ticketSummary = buildTicketSummary(payload);
        String qrEndpoint = "/api/v1/qr-codes/ticket/" + bookingCode;

        String message = "Xin chào " + customerName + ",\n\n"
                + "Vé của booking " + bookingCode + " đã được phát hành.\n"
                + ticketSummary
                + "QR vé: " + qrEndpoint + "\n\n"
                + "Vui lòng đưa mã QR hoặc mã vé cho nhân viên rạp khi check-in.";

        sendNotificationUseCase.execute(new SendNotificationCommand(
                recipientEmail,
                "Vé xem phim đã sẵn sàng - " + bookingCode,
                message,
                "EMAIL",
                "TICKET_BOOKED",
                sourceEventId,
                topic,
                null
        ));
    }

    private void handleSeatHoldCreated(JsonNode payload, String topic, String rawMessage) {
        String recipientEmail = recipientResolver.resolveEmail(payload);
        String customerName = recipientResolver.resolveCustomerName(payload);
        String holdToken = text(payload, "holdToken", "unknown-hold-token");
        String expiresAt = text(payload, "expiresAt", "thời gian giữ ghế đã cấu hình");
        String sourceEventId = sourceEventId(payload, rawMessage, holdToken, "seat-hold-created");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Bạn đang giữ ghế với mã giữ chỗ: " + holdToken + ".\n"
                + "Thời gian hết hạn: " + expiresAt + ".\n"
                + "Vui lòng hoàn tất thanh toán trước khi hết hạn để xác nhận vé.";

        sendNotificationUseCase.execute(new SendNotificationCommand(
                recipientEmail,
                "Bạn đang giữ ghế - " + holdToken,
                message,
                "EMAIL",
                "SEAT_HOLD_CREATED",
                sourceEventId,
                topic,
                null
        ));
    }

    private void handleSeatHoldExpired(JsonNode payload, String topic, String rawMessage) {
        String recipientEmail = recipientResolver.resolveEmail(payload);
        String customerName = recipientResolver.resolveCustomerName(payload);
        String holdToken = text(payload, "holdToken", "unknown-hold-token");
        String sourceEventId = sourceEventId(payload, rawMessage, holdToken, "seat-hold-expired");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Mã giữ chỗ " + holdToken + " đã hết hạn.\n"
                + "Các ghế trong lượt giữ này đã được mở lại cho người dùng khác.\n"
                + "Bạn có thể chọn lại ghế và đặt vé mới nếu vẫn muốn tiếp tục.";

        sendNotificationUseCase.execute(new SendNotificationCommand(
                recipientEmail,
                "Mã giữ ghế đã hết hạn - " + holdToken,
                message,
                "EMAIL",
                "SEAT_HOLD_EXPIRED",
                sourceEventId,
                topic,
                null
        ));
    }

    private void scheduleShowtimeReminder(
            JsonNode payload,
            String recipientEmail,
            String customerName,
            String bookingCode,
            String movieTitle,
            String showtimeLabel,
            String confirmedSourceEventId
    ) {
        Optional<LocalDateTime> reminderAt = showtimeReminderPlanner.resolveReminderAt(payload);
        if (reminderAt.isEmpty()) {
            log.info("Skip showtime reminder for bookingCode={} because event has no showtimeStartAt/showDate/startTime/showtimeId", bookingCode);
            return;
        }

        String message = "Xin chào " + customerName + ",\n\n"
                + "Suất chiếu của bạn sắp bắt đầu.\n"
                + "Mã booking: " + bookingCode + "\n"
                + "Phim: " + movieTitle + "\n"
                + "Thời gian: " + showtimeLabel + "\n\n"
                + "Vui lòng đến rạp sớm để check-in và ổn định chỗ ngồi.";

        sendNotificationUseCase.execute(new SendNotificationCommand(
                recipientEmail,
                "Nhắc lịch xem phim - " + bookingCode,
                message,
                "EMAIL",
                "SHOWTIME_REMINDER",
                confirmedSourceEventId + ":reminder",
                Constants.TOPIC_SHOWTIME_REMINDER,
                reminderAt.get()
        ));
    }

    private String buildTicketSummary(JsonNode payload) {
        JsonNode tickets = payload.path("tickets");
        if (!tickets.isArray() || tickets.size() == 0) {
            return "Danh sách vé: chưa có thông tin chi tiết.\n";
        }

        StringBuilder builder = new StringBuilder("Danh sách vé:\n");
        tickets.forEach(ticket -> {
            String ticketCode = text(ticket, "ticketCode", "unknown-ticket");
            String seatCode = text(ticket, "seatCode", "unknown-seat");
            builder.append("- ").append(ticketCode).append(" / Ghế ").append(seatCode).append("\n");
        });
        return builder.toString();
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
