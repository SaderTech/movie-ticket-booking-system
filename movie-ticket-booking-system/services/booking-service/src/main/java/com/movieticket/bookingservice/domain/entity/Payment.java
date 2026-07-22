package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_payments_transaction_ref", columnNames = {"transaction_ref"})
       },
       indexes = {
            @Index(name = "idx_payments_booking_id", columnList = "booking_id"),
            @Index(name = "idx_payments_status_created", columnList = "status, created_at")
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "transaction_ref", length = 100)
    private String transactionRef;

    @Column(name = "method", nullable = false, length = 30)
    private String method;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "vnp_transaction_no", length = 50)
    private String vnpTransactionNo;

    @Column(name = "raw_response", columnDefinition = "TEXT")
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

    public void setVnPayDetails(String vnpTransactionNo, String rawResponse) {
        this.vnpTransactionNo = vnpTransactionNo;
        this.rawResponse = rawResponse;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}