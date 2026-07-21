package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class SagaTransaction {
    private Long id;
    private String sagaId;
    private Long bookingId;
    private SagaStatus status;
    private String currentStep;
    private String failureReason;
    private LocalDateTime createdAt;
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
}
