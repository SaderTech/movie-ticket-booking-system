package com.movieticket.bookingservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingConfirmedEvent {
    private String bookingCode;
    private Long userId;
    private Long showtimeId;
    private BigDecimal totalAmount;
    private String paymentMethod;
}
