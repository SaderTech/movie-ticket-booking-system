package com.movieticket.bookingservice.infrastructure.adapter;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.Payment;
import com.movieticket.bookingservice.domain.enums.PaymentStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class PaymentAdapter {

    public Payment processPayment(Booking booking, String paymentMethod) {
        log.debug("Processing payment for booking {} with method {}", booking.getBookingCode(), paymentMethod);

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

        log.info("Payment successful for booking {}: txn={}", booking.getBookingCode(), payment.getTransactionRef());
        return payment;
    }
}
