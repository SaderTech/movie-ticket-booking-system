package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {
    private Long id;
    private Long bookingId;
    private Long showtimeId;
    private String seatCode;
    private String seatType;
    private BigDecimal price;
    private BookingSeatStatus status;
    private LocalDateTime createdAt;
}
