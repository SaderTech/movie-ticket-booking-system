package com.movieticket.bookingservice;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.Payment;
import com.movieticket.bookingservice.infrastructure.adapter.PaymentAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentAdapterTest {

    private PaymentAdapter paymentAdapter;

    @BeforeEach
    void setUp() {
        paymentAdapter = new PaymentAdapter();
        ReflectionTestUtils.setField(paymentAdapter, "vnpTmnCode", "TEST_TMN");
        ReflectionTestUtils.setField(paymentAdapter, "vnpHashSecret", "TEST_SECRET");
        ReflectionTestUtils.setField(paymentAdapter, "vnpUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(paymentAdapter, "vnpDefaultReturnUrl", "http://localhost:8085/api/bookings/vnpay-return");
        ReflectionTestUtils.setField(paymentAdapter, "allowedReturnHosts", "localhost,127.0.0.1");
    }

    @Test
    void shouldUseRequestedReturnUrlWhenHostIsAllowed() {
        Booking booking = Booking.builder()
                .bookingCode("BK_TEST")
                .totalAmount(BigDecimal.valueOf(100000))
                .build();
        Payment payment = Payment.builder()
                .transactionRef("TXN_TEST")
                .amount(BigDecimal.valueOf(100000))
                .build();

        String paymentUrl = paymentAdapter.createPaymentUrl(booking, payment, "127.0.0.1", "http://localhost:3000/callback");

        assertTrue(paymentUrl.contains("http%3A%2F%2Flocalhost%3A3000%2Fcallback"));
    }

    @Test
    void shouldRejectReturnUrlWhenHostIsNotAllowed() {
        Booking booking = Booking.builder()
                .bookingCode("BK_TEST")
                .totalAmount(BigDecimal.valueOf(100000))
                .build();
        Payment payment = Payment.builder()
                .transactionRef("TXN_TEST")
                .amount(BigDecimal.valueOf(100000))
                .build();

        assertThrows(IllegalArgumentException.class,
                () -> paymentAdapter.createPaymentUrl(booking, payment, "127.0.0.1", "http://evil.com/callback"));
    }
}
