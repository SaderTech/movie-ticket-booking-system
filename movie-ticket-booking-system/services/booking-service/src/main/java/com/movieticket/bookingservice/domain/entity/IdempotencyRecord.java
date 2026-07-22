package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.IdempotencyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_records",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_idempotency_records_key", columnNames = {"idempotency_key"})
       },
       indexes = {
            @Index(name = "idx_idempotency_records_expires_at", columnList = "expires_at")
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 150)
    private String idempotencyKey;

    @Column(name = "request_hash", length = 64)
    private String requestHash;

    @Column(name = "operation_type", length = 50)
    private String operationType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IdempotencyStatus status;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public void succeed(String response) {
        status = IdempotencyStatus.SUCCEEDED;
        responseBody = response;
    }

    public void fail() {
        status = IdempotencyStatus.FAILED;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}