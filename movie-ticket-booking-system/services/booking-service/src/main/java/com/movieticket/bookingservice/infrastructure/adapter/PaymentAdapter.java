package com.movieticket.bookingservice.infrastructure.adapter;

import com.movieticket.bookingservice.domain.entity.Booking;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PaymentAdapter {

    @Value("${vnpay.tmn-code}")
    private String vnpTmnCode;

    @Value("${vnpay.hash-secret}")
    private String vnpHashSecret;

    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.return-url}")
    private String vnpDefaultReturnUrl;

    @Value("${vnpay.allowed-return-hosts:localhost,127.0.0.1}")
    private String allowedReturnHosts;

    public String createPaymentUrl(Booking booking, com.movieticket.bookingservice.domain.entity.Payment payment,
                                   String ipAddress, String requestedReturnUrl) {
        String resolvedReturnUrl = resolveReturnUrl(requestedReturnUrl);
        String orderInfo = "Thanh toan ve xem phim - " + booking.getBookingCode();

        String paymentUrl = VnPayUtil.buildPaymentUrl(
                vnpUrl, vnpTmnCode, vnpHashSecret,
                payment.getTransactionRef(),
                booking.getTotalAmount().longValue(),
                orderInfo,
                resolvedReturnUrl,
                ipAddress
        );

        log.info("VNPay payment URL created for booking {} with returnUrl {}", booking.getBookingCode(), resolvedReturnUrl);
        return paymentUrl;
    }

    private String resolveReturnUrl(String requestedReturnUrl) {
        if (requestedReturnUrl == null || requestedReturnUrl.isBlank()) {
            return vnpDefaultReturnUrl;
        }

        try {
            URI uri = URI.create(requestedReturnUrl);
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new IllegalArgumentException("Only http/https URLs are allowed");
            }
            String host = uri.getHost();
            if (host == null) {
                throw new IllegalArgumentException("Missing host in return URL");
            }

            Set<String> allowedHosts = Arrays.stream(allowedReturnHosts.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .collect(Collectors.toSet());

            if (!allowedHosts.contains(host.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Return URL host is not allowed");
            }

            return requestedReturnUrl;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid return URL: " + e.getMessage(), e);
        }
    }

    public boolean verifyReturn(Map<String, String> params) {
        return VnPayUtil.verifyReturn(vnpHashSecret, params);
    }
}
