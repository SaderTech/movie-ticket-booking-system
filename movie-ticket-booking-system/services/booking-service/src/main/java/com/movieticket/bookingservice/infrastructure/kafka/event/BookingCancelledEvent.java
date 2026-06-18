package com.movieticket.bookingservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent {
    private String bookingCode;
    private Long userId;
    private String reason;
}
