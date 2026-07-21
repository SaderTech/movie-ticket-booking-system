package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.IdempotencyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class IdempotencyRecord {
    private Long id;
    private String idempotencyKey;
    private String requestHash;
    private String operationType;
    private IdempotencyStatus status;
    private String responseBody;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    public void succeed(String response) {
        status = IdempotencyStatus.SUCCEEDED;
        responseBody = response;
    }

    public void fail() {
        status = IdempotencyStatus.FAILED;
    }
}
