package com.movieticket.showtimeservice.application.dto.response;


import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatAvailabilityResponse {


    private Long showtimeId;


    private Integer requestedSeats;


    private Integer availableSeats;


    private Boolean available;

}