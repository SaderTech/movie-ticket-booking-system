package com.movieticket.bookingservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldSeatsResponse {
    private String holdToken;
    private LocalDateTime expiresAt;
    private List<SeatHoldSeatDto> seats;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SeatHoldSeatDto {
        private String seatCode;
        private String seatType;
    }
}
