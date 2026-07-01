package com.movieticket.bookingservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldSeat {
    private Long id;
    private Long holdId;
    private Long showtimeId;
    private String seatCode;
    private LocalDateTime createdAt;
}
