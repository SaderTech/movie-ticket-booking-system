package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.SagaStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "saga_transactions",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_saga_transactions_saga_id", columnNames = {"saga_id"})
       },
       indexes = {
            @Index(name = "idx_saga_transactions_booking_id", columnList = "booking_id"),
            @Index(name = "idx_saga_transactions_status_updated", columnList = "status, updated_at")
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SagaTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "saga_id", nullable = false, length = 100)
    private String sagaId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SagaStatus status;

    @Column(name = "current_step", length = 100)
    private String currentStep;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void startStep(String step) {
        status = SagaStatus.STARTED;
        currentStep = step;
    }

    public void complete() {
        status = SagaStatus.COMPLETED;
    }

    public void fail(String reason) {
        status = SagaStatus.FAILED;
        failureReason = reason;
    }

    public void startCompensation() {
        status = SagaStatus.COMPENSATING;
    }

    public void compensate() {
        status = SagaStatus.COMPENSATED;
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