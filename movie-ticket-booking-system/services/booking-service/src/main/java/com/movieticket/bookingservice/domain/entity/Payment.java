package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class Payment {
    private Long id;
    private Long bookingId;
    private String transactionRef;
    private String method;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String failureReason;
    private String rawResponse;

    public void assignToBooking(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void markPaid(String txnRef) {
        if (status == PaymentStatus.PAID) {
            return;
        }
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Only pending payment can be marked paid, current: " + status);
        }
        status = PaymentStatus.PAID;
        paidAt = LocalDateTime.now();
        transactionRef = txnRef;
        updatedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        if (status == PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot fail an already paid payment");
        }
        status = PaymentStatus.FAILED;
        failureReason = reason;
        updatedAt = LocalDateTime.now();
    }
}
