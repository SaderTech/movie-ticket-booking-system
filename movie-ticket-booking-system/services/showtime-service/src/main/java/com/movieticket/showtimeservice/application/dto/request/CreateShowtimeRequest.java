package com.movieticket.showtimeservice.application.dto.request;



import com.movieticket.showtimeservice.domain.model.ShowtimeStatus;
import jakarta.validation.constraints.*;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class CreateShowtimeRequest {

    @NotNull(message = "Movie ID is required")
    private Long movieId;

    @NotNull(message = "Cinema ID is required")
    private Long cinemaId;

    @NotNull(message = "Room ID is required")
    private Long roomId;

    @NotNull(message = "Show date is required")
    private LocalDate showDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal price;

    @NotNull(message = "Available seats is required")
    @Min(value = 0)
    private Integer availableSeats;

    @NotNull(message = "Status is required")
    private ShowtimeStatus status;

}
