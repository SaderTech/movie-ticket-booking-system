package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class SeatHold {
    private Long id;
    private String holdToken;
    private Long userId;
    private Long showtimeId;
    private SeatHoldStatus status;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<SeatHoldSeat> seats = new ArrayList<>();

    public boolean isActive() {
        return status == SeatHoldStatus.ACTIVE && expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }

    public void convert() {
        if (status != SeatHoldStatus.ACTIVE) {
            throw new IllegalStateException("Only active hold can be converted, current: " + status);
        }
        status = SeatHoldStatus.CONVERTED;
        updatedAt = LocalDateTime.now();
    }

    public void expire() {
        if (status != SeatHoldStatus.ACTIVE) {
            return;
        }
        status = SeatHoldStatus.EXPIRED;
        updatedAt = LocalDateTime.now();
    }

    public void release() {
        if (status != SeatHoldStatus.ACTIVE) {
            return;
        }
        status = SeatHoldStatus.RELEASED;
        updatedAt = LocalDateTime.now();
    }
}
