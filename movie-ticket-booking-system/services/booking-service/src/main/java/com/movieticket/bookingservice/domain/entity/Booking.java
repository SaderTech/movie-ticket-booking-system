package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
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

    public void markPendingPayment() {
        status = BookingStatus.PENDING_PAYMENT;
    }

    public void confirm() {
        if (status != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Cannot confirm booking with status: " + status);
        }
        status = BookingStatus.CONFIRMED;
        updatedAt = LocalDateTime.now();
        seats.forEach(BookingSeat::confirm);
    }

    public void fail(String reason) {
        if (status == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Cannot fail a confirmed booking");
        }
        status = BookingStatus.FAILED;
        updatedAt = LocalDateTime.now();
        seats.forEach(BookingSeat::cancel);
    }

    public void cancel() {
        if (status == BookingStatus.CANCELLED) {
            return;
        }
        if (status == BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Confirmed booking cannot be cancelled by user, use admin flow");
        }
        status = BookingStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
        seats.forEach(BookingSeat::cancel);
    }
}
