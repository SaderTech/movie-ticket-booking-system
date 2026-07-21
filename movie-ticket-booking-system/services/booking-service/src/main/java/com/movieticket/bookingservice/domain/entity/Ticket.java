package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@Builder
public class Ticket {
    private Long id;
    private String ticketCode;
    private Long bookingId;
    private Long userId;
    private Long showtimeId;
    private Long movieId;
    private String movieTitle;
    private String moviePosterUrl;
    private Long cinemaId;
    private String cinemaName;
    private Long hallId;
    private String hallName;
    private String seatCode;
    private String seatType;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal price;
    private String qrPayload;
    private TicketStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void issue() {
        if (status != null) {
            throw new IllegalStateException("Ticket already has status: " + status);
        }
        status = TicketStatus.ACTIVE;
        issuedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == TicketStatus.CANCELLED) {
            return;
        }
        if (status == TicketStatus.USED) {
            throw new IllegalStateException("Cannot cancel a used ticket");
        }
        status = TicketStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
    }
}
