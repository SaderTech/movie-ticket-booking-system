package com.movieticket.showtimeservice.application.dto.response;

import com.movieticket.showtimeservice.domain.model.ShowtimeStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowtimeResponse {

    private Long id;

    private Long movieId;

    private Long cinemaId;

    private Long roomId;

    private LocalDate showDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private BigDecimal price;

    private Integer availableSeats;

    private ShowtimeStatus status;

}