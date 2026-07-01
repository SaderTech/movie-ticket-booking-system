package com.movieticket.bookingservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {
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
    private String status;
    private LocalDateTime issuedAt;
}
