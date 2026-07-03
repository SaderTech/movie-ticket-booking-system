package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
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

    public void assignToBooking(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void confirm() {
        if (status != BookingSeatStatus.PENDING) {
            throw new IllegalStateException("Only pending seat can be confirmed, current: " + status);
        }
        status = BookingSeatStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == BookingSeatStatus.CANCELLED) {
            return;
        }
        status = BookingSeatStatus.CANCELLED;
    }
}
