package com.movieticket.notificationservice.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.notificationservice.application.command.SendNotificationCommand;
import com.movieticket.notificationservice.application.usecase.SendNotificationUseCase;
import com.movieticket.notificationservice.config.Constants;
import com.movieticket.notificationservice.domain.service.NotificationRecipientResolver;
import com.movieticket.notificationservice.domain.service.ShowtimeReminderPlanner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        String seatLabel = seatLabel(payload);
        String sourceEventId = sourceEventId(payload, rawMessage, bookingCode, "booking-confirmed");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Bạn đã đặt vé thành công. Vé điện tử sẽ được phát hành ngay sau khi hệ thống tạo ticket code.\n"
                + "Mã đặt vé: " + bookingCode + "\n"
                + "Phim: " + movieTitle + "\n"
                + "Suất chiếu: " + showtimeLabel + "\n"
                + "Ghế: " + seatLabel + "\n"
                + "Tổng tiền: " + totalAmount + "\n\n"
                + "Khi ticket code được phát hành, hệ thống sẽ gửi email vé kèm mã QR theo từng ticket code.";

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

        scheduleShowtimeReminder(payload, recipientEmail, customerName, bookingCode, movieTitle, showtimeLabel, seatLabel, sourceEventId);
    }

    private void handleBookingCancelled(JsonNode payload, String topic, String rawMessage) {
        String recipientEmail = recipientResolver.resolveEmail(payload);
        String customerName = recipientResolver.resolveCustomerName(payload);
        String bookingCode = text(payload, "bookingCode", "unknown-booking");
        String reason = text(payload, "reason", "Không có lý do cụ thể");
        String sourceEventId = sourceEventId(payload, rawMessage, bookingCode, "booking-cancelled");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Mã đặt vé: " + bookingCode + "\n"
                + "Trạng thái: Đã hủy\n"
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
        String movieTitle = text(payload, "movieTitle", "bộ phim bạn đã chọn");
        String showtimeLabel = showtimeReminderPlanner.buildShowtimeLabel(payload);
        String seatLabel = seatLabel(payload);
        String ticketCodeLabel = ticketCodeLabel(payload);
        String sourceEventId = sourceEventId(payload, rawMessage, bookingCode, "ticket-booked");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Vé của bạn đã được phát hành thành công.\n"
                + "Mã đặt vé: " + bookingCode + "\n"
                + "Phim: " + movieTitle + "\n"
                + "Suất chiếu: " + showtimeLabel + "\n"
                + "Ghế: " + seatLabel + "\n"
                + "Mã vé/QR: " + ticketCodeLabel + "\n\n"
                + "Mã QR của từng ticket code đã được đính kèm trong email. Vui lòng đưa đúng mã QR hoặc mã vé cho nhân viên rạp khi check-in.";

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
        String seatLabel = seatLabel(payload);
        String sourceEventId = sourceEventId(payload, rawMessage, holdToken, "seat-hold-created");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Bạn đang giữ ghế với mã giữ chỗ: " + holdToken + ".\n"
                + "Ghế: " + seatLabel + "\n"
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
        String seatLabel = seatLabel(payload);
        String sourceEventId = sourceEventId(payload, rawMessage, holdToken, "seat-hold-expired");

        String message = "Xin chào " + customerName + ",\n\n"
                + "Mã giữ chỗ " + holdToken + " đã hết hạn.\n"
                + "Ghế: " + seatLabel + "\n"
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
            String seatLabel,
            String confirmedSourceEventId
    ) {
        Optional<LocalDateTime> reminderAt = showtimeReminderPlanner.resolveReminderAt(payload);
        if (reminderAt.isEmpty()) {
            log.info("Skip showtime reminder for bookingCode={} because event has no showtimeStartAt/showDate/startTime/showtimeId", bookingCode);
            return;
        }

        String message = "Xin chào " + customerName + ",\n\n"
                + "Suất chiếu của bạn sắp bắt đầu.\n"
                + "Mã đặt vé: " + bookingCode + "\n"
                + "Phim: " + movieTitle + "\n"
                + "Suất chiếu: " + showtimeLabel + "\n"
                + "Ghế: " + seatLabel + "\n\n"
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

    private String seatLabel(JsonNode payload) {
        List<String> seats = new ArrayList<>();
        JsonNode seatCodes = payload.path("seatCodes");
        if (seatCodes.isArray()) {
            seatCodes.forEach(node -> addIfPresent(seats, node.asText(null)));
        }

        JsonNode tickets = payload.path("tickets");
        if (tickets.isArray()) {
            tickets.forEach(ticket -> addIfPresent(seats, ticket.path("seatCode").asText(null)));
        }

        return joinDistinct(seats, "chưa có thông tin ghế");
    }

    private String ticketCodeLabel(JsonNode payload) {
        List<String> ticketCodes = new ArrayList<>();
        JsonNode tickets = payload.path("tickets");
        if (tickets.isArray()) {
            tickets.forEach(ticket -> addIfPresent(ticketCodes, ticket.path("ticketCode").asText(null)));
        }
        addIfPresent(ticketCodes, payload.path("ticketCode").asText(null));
        return joinDistinct(ticketCodes, "chưa có ticket code");
    }

    private void addIfPresent(List<String> values, String value) {
        if (value != null && !value.isBlank()) {
            values.add(value.trim());
        }
    }

    private String joinDistinct(List<String> values, String fallback) {
        Set<String> distinct = new LinkedHashSet<>(values);
        if (distinct.isEmpty()) {
            return fallback;
        }
        return String.join(", ", distinct);
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
