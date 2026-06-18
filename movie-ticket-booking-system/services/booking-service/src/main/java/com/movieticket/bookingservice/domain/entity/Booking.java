package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    private Long id;
    private String bookingCode;
    private Long userId;
    private Long showtimeId;
    private BigDecimal totalAmount;
    private BookingStatus status;
    private String holdToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Builder.Default
    private List<BookingSeat> seats = new ArrayList<>();
}
