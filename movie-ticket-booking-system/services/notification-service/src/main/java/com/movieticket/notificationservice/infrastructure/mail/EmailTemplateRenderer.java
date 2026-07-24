package com.movieticket.notificationservice.infrastructure.mail;

import com.movieticket.notificationservice.domain.entity.NotificationLog;
import com.movieticket.notificationservice.domain.enums.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class EmailTemplateRenderer {

    public String render(NotificationLog log, String actionUrl, boolean qrAttached) {
        String accent = accentColor(log.getType());
        String headline = headline(log.getType());
        String badge = badge(log.getType());
        String body = renderBody(log.getMessage());
        String cta = renderCallToAction(log.getType(), actionUrl, qrAttached);

        return """
                <!doctype html>
                <html lang=\"vi\">
                <head>
                  <meta charset=\"UTF-8\" />
                  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />
                  <title>%s</title>
                </head>
                <body style=\"margin:0;background:#f5f7fb;font-family:Arial,Helvetica,sans-serif;color:#111827;\">
                  <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"background:#f5f7fb;padding:28px 12px;\">
                    <tr>
                      <td align=\"center\">
                        <table role=\"presentation\" width=\"100%%\" cellspacing=\"0\" cellpadding=\"0\" style=\"max-width:640px;background:#ffffff;border-radius:24px;overflow:hidden;box-shadow:0 18px 45px rgba(15,23,42,.10);\">
                          <tr>
                            <td style=\"background:%s;padding:28px 32px;color:#ffffff;\">
                              <div style=\"font-size:12px;letter-spacing:.14em;text-transform:uppercase;opacity:.88;\">Movie Ticket</div>
                              <h1 style=\"font-size:28px;line-height:1.25;margin:10px 0 0;\">%s</h1>
                              <div style=\"display:inline-block;margin-top:14px;padding:7px 12px;border-radius:999px;background:rgba(255,255,255,.18);font-size:12px;font-weight:700;\">%s</div>
                            </td>
                          </tr>
                          <tr>
                            <td style=\"padding:30px 32px 16px;\">
                              %s
                              %s
                            </td>
                          </tr>
                          <tr>
                            <td style=\"padding:0 32px 30px;\">
                              <div style=\"border-top:1px solid #e5e7eb;padding-top:18px;color:#6b7280;font-size:13px;line-height:1.6;\">
                                Email này được gửi tự động từ hệ thống Movie Ticket. Vui lòng không chia sẻ mã vé hoặc mã QR cho người khác.
                              </div>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                escape(log.getSubject()),
                accent,
                escape(headline),
                escape(badge),
                body,
                cta
        );
    }

    private String renderBody(String message) {
        if (message == null || message.isBlank()) {
            return "<p style=\"margin:0 0 14px;font-size:15px;line-height:1.7;\">Không có nội dung.</p>";
        }
        return Arrays.stream(message.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .map(line -> "<p style=\"margin:0 0 12px;font-size:15px;line-height:1.7;\">" + decorateLine(line) + "</p>")
                .collect(Collectors.joining("\n"));
    }

    private String decorateLine(String line) {
        String escaped = escape(line);
        if (line.startsWith("Mã booking:") || line.startsWith("Mã đặt vé:") || line.startsWith("Mã vé:")
                || line.startsWith("Mã vé/QR:") || line.startsWith("Tổng tiền:") || line.startsWith("Ghế:")
                || line.startsWith("Phim:") || line.startsWith("Suất chiếu:") || line.startsWith("Thời gian:")
                || line.startsWith("Thanh toán:") || line.startsWith("Trạng thái:") || line.startsWith("Lý do:")) {
            int idx = escaped.indexOf(':');
            if (idx > -1) {
                return "<strong>" + escaped.substring(0, idx + 1) + "</strong>" + escaped.substring(idx + 1);
            }
        }
        if (line.startsWith("- ")) {
            return "• " + escaped.substring(2);
        }
        return escaped;
    }

    private String renderCallToAction(NotificationType type, String actionUrl, boolean qrAttached) {
        StringBuilder html = new StringBuilder();
        if (actionUrl != null && !actionUrl.isBlank()) {
            html.append("<div style=\"margin:22px 0 6px;\"><a href=\"")
                    .append(escape(actionUrl))
                    .append("\" style=\"display:inline-block;background:#111827;color:#ffffff;text-decoration:none;padding:12px 18px;border-radius:12px;font-weight:700;font-size:14px;\">")
                    .append(escape(callToActionLabel(type)))
                    .append("</a></div>");
        }
        if (qrAttached) {
            html.append("<div style=\"margin-top:14px;padding:14px 16px;border-radius:16px;background:#f8fafc;border:1px solid #e5e7eb;color:#374151;font-size:14px;line-height:1.6;\">")
                    .append("Mã QR được tạo theo từng ticket code và đã được đính kèm trong email. Bạn có thể mở file PNG hoặc đưa mã QR cho nhân viên rạp khi check-in.")
                    .append("</div>");
        }
        return html.toString();
    }

    private String headline(NotificationType type) {
        if (type == null) return "Thông báo từ Movie Ticket";
        return switch (type) {
            case BOOKING_CONFIRMATION -> "Đặt vé thành công";
            case TICKET_BOOKED -> "Vé điện tử đã sẵn sàng";
            case SHOWTIME_REMINDER -> "Sắp đến giờ chiếu";
            case BOOKING_CANCELLED -> "Booking đã được hủy";
            case PAYMENT_SUCCESS -> "Thanh toán thành công";
            case PAYMENT_FAILED -> "Thanh toán chưa thành công";
            case PAYMENT_REFUND_REQUIRED -> "Giao dịch đang chờ hoàn tiền";
            case SEAT_HOLD_CREATED -> "Ghế đã được giữ tạm thời";
            case SEAT_HOLD_EXPIRED -> "Mã giữ ghế đã hết hạn";
            case TICKET_CANCELLED -> "Vé đã được hủy";
            case SYSTEM_ALERT -> "Thông báo hệ thống";
        };
    }

    private String badge(NotificationType type) {
        return type == null ? "SYSTEM_ALERT" : type.name();
    }

    private String accentColor(NotificationType type) {
        if (type == null) return "#4f46e5";
        return switch (type) {
            case BOOKING_CONFIRMATION, TICKET_BOOKED, PAYMENT_SUCCESS -> "#16a34a";
            case PAYMENT_FAILED, BOOKING_CANCELLED, TICKET_CANCELLED, SEAT_HOLD_EXPIRED -> "#dc2626";
            case SHOWTIME_REMINDER, SEAT_HOLD_CREATED -> "#f59e0b";
            case PAYMENT_REFUND_REQUIRED -> "#7c3aed";
            case SYSTEM_ALERT -> "#2563eb";
        };
    }

    private String callToActionLabel(NotificationType type) {
        if (type == null) return "Mở hệ thống";
        return switch (type) {
            case TICKET_BOOKED -> "Mở QR vé đầu tiên";
            case BOOKING_CONFIRMATION, SHOWTIME_REMINDER -> "Xem thông tin đặt vé";
            case PAYMENT_SUCCESS, PAYMENT_FAILED, PAYMENT_REFUND_REQUIRED -> "Xem lịch sử thanh toán";
            case BOOKING_CANCELLED, TICKET_CANCELLED -> "Xem lịch sử đặt vé";
            case SEAT_HOLD_CREATED -> "Hoàn tất thanh toán";
            case SEAT_HOLD_EXPIRED -> "Chọn lại ghế";
            case SYSTEM_ALERT -> "Mở hệ thống";
        };
    }

    private String escape(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
