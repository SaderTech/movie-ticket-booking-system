package com.movieticket.bookingservice.domain.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentRefundRequiredEvent(
        String eventId,
        String transactionRef,
        String bookingCode,
        Long userId,
        String customerEmail,
        String customerName,
        BigDecimal amount,
        String reason,
        LocalDateTime occurredAt
) implements DomainEvent {

    public PaymentRefundRequiredEvent(String transactionRef, String bookingCode, Long userId,
                                      BigDecimal amount, String reason) {
        this(UUID.randomUUID().toString(), transactionRef, bookingCode, userId, null, null, amount, reason, LocalDateTime.now());
    }

    public PaymentRefundRequiredEvent(String transactionRef, String bookingCode, Long userId,
                                      String customerEmail, String customerName, BigDecimal amount, String reason) {
        this(UUID.randomUUID().toString(), transactionRef, bookingCode, userId, customerEmail, customerName,
                amount, reason, LocalDateTime.now());
    }

    @Override
    public String aggregateId() {
        return transactionRef;
    }

    @Override
    public String eventType() {
        return "PAYMENT_REFUND_REQUIRED";
    }
}
