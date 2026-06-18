package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.IdempotencyStatus;
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
public class IdempotencyRecord {
    private Long id;
    private String idempotencyKey;
    private String requestHash;
    private String operationType;
    private IdempotencyStatus status;
    private String responseBody;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}
