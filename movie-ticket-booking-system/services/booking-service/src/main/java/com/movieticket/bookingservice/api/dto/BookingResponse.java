package com.movieticket.bookingservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {
    private Long id;
    private String bookingCode;
    private Long userId;
    private Long showtimeId;
    private BigDecimal totalAmount;
    private String status;
    private String holdToken;
    private List<BookingSeatDto> seats;
    private List<TicketResponse> tickets;
    private PaymentDto payment;
    private String paymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BookingSeatDto {
        private String seatCode;
        private String seatType;
        private BigDecimal price;
        private String status;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PaymentDto {
        private Long id;
        private String transactionRef;
        private String method;
        private BigDecimal amount;
        private String status;
        private LocalDateTime paidAt;
    }
}
