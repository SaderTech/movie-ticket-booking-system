package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.SagaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
}
