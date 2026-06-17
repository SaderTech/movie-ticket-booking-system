package com.movieticket.bookingservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldCreatedEvent {
    private String holdToken;
    private Long userId;
    private Long showtimeId;
    private List<String> seatCodes;
    private LocalDateTime expiresAt;
}
