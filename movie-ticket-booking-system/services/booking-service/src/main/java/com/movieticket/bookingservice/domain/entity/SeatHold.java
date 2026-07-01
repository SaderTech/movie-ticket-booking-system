package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
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
}
