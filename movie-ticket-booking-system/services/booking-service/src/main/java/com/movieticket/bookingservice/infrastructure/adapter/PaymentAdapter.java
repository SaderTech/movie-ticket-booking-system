package com.movieticket.bookingservice.infrastructure.adapter;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.Payment;
import com.movieticket.bookingservice.domain.enums.PaymentStatus;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@Slf4j
public class PaymentAdapter {

    @Setter
    private HttpServletRequest request;

    public String getClientIpAddress() {
        if (request == null) return "127.0.0.1";
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.return-url}")
    private String vnpReturnUrl;

    public Payment processPayment(Booking booking, String paymentMethod) {
        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .transactionRef("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                .method(paymentMethod)
                .amount(booking.getTotalAmount())
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        log.info("Mock payment successful for booking {}: txn={}", booking.getBookingCode(), payment.getTransactionRef());
        return payment;
    }

    public String createPaymentUrl(Booking booking, Payment payment, String ipAddress) {
        String orderInfo = "Thanh toan ve xem phim - " + booking.getBookingCode();

        String paymentUrl = VnPayUtil.buildPaymentUrl(
                vnpUrl, vnpTmnCode, vnpHashSecret,
                payment.getTransactionRef(),
                booking.getTotalAmount().longValue(),
                orderInfo,
                vnpReturnUrl,
                ipAddress
        );

        log.info("VNPay payment URL created for booking {}: {}", booking.getBookingCode(), paymentUrl);
        return paymentUrl;
    }

    public boolean verifyReturn(Map<String, String> params) {
        return VnPayUtil.verifyReturn(vnpHashSecret, params);
    }
}
