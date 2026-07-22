package com.movieticket.showtimeservice.interfaces.rest;


import com.movieticket.showtimeservice.application.dto.request.CreateShowtimeRequest;
import com.movieticket.showtimeservice.application.dto.request.UpdateShowtimeRequest;
import com.movieticket.showtimeservice.application.dto.response.ShowtimeResponse;
import com.movieticket.showtimeservice.application.service.ShowtimeService;
import com.movieticket.showtimeservice.application.dto.request.ReleaseSeatRequest;
import com.movieticket.showtimeservice.application.dto.response.SeatAvailabilityResponse;

import jakarta.validation.Valid;
import com.movieticket.showtimeservice.application.dto.request.ReserveSeatRequest;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;



@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {



    private final ShowtimeService showtimeService;



    /**
     * Lấy tất cả suất chiếu
     *
     * GET /api/showtimes
     */
    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> getAllShowtimes() {

        return ResponseEntity.ok(
                showtimeService.getAllShowtimes()
        );

    }





    /**
     * Lấy suất chiếu theo ID
     *
     * GET /api/showtimes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> getShowtimeById(
            @PathVariable("id") Long id
    ) {


        return ResponseEntity.ok(
                showtimeService.getShowtimeById(id)
        );

    }





    /**
     * Lấy suất chiếu theo Movie
     *
     * GET /api/showtimes/movie/{movieId}
     */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByMovie(
            @PathVariable("movieId") Long movieId
    ) {


        return ResponseEntity.ok(
                showtimeService.getShowtimesByMovieId(movieId)
        );

    }





    /**
     * Lấy suất chiếu theo Cinema
     *
     * GET /api/showtimes/cinema/{cinemaId}
     */
    @GetMapping("/cinema/{cinemaId}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByCinema(
            @PathVariable("cinemaId") Long cinemaId
    ) {


        return ResponseEntity.ok(
                showtimeService.getShowtimesByCinemaId(cinemaId)
        );

    }





    /**
     * Lấy suất chiếu theo ngày
     *
     * GET /api/showtimes/date/{date}
     *
     * Ví dụ:
     * /api/showtimes/date/2026-07-25
     */
    @GetMapping("/date/{date}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByDate(
            @PathVariable("date") LocalDate date
    ) {


        return ResponseEntity.ok(
                showtimeService.getShowtimesByDate(date)
        );

    }





    /**
     * Lấy suất chiếu theo Movie + Date
     *
     * GET /api/showtimes/movie/{movieId}/date/{date}
     *
     * Đây là API chính cho Booking Service
     */
    @GetMapping("/movie/{movieId}/date/{date}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByMovieAndDate(
            @PathVariable("movieId") Long movieId,
            @PathVariable("date") LocalDate date
    ) {


        return ResponseEntity.ok(
                showtimeService.getShowtimesByMovieAndDate(
                        movieId,
                        date
                )
        );

    }



    /**
     * Lấy suất chiếu còn vé theo Movie + Date
     *
     * GET /api/showtimes/movie/{movieId}/date/{date}/available
     *
     * Dùng cho Booking Service
     */
    @GetMapping("/movie/{movieId}/date/{date}/available")
    public ResponseEntity<List<ShowtimeResponse>> getAvailableShowtimes(
            @PathVariable("movieId") Long movieId,
            @PathVariable("date") LocalDate date
    ) {


        return ResponseEntity.ok(
                showtimeService.getAvailableShowtimes(
                        movieId,
                        date
                )
        );

    }





    /**
     * Tạo suất chiếu mới
     *
     * POST /api/showtimes
     */
    @PostMapping
    public ResponseEntity<ShowtimeResponse> createShowtime(
            @Valid @RequestBody CreateShowtimeRequest request
    ) {


        ShowtimeResponse response =
                showtimeService.createShowtime(request);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }





    /**
     * Cập nhật suất chiếu
     *
     * PUT /api/showtimes/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> updateShowtime(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateShowtimeRequest request
    ) {


        ShowtimeResponse response =
                showtimeService.updateShowtime(
                        id,
                        request
                );


        return ResponseEntity.ok(response);

    }


    /**
     * Kiểm tra số ghế còn trống
     *
     * GET /api/showtimes/{id}/availability?quantity=3
     *
     * Dùng trước khi Booking Service reserve
     */
    @GetMapping("/{id}/availability")
    public ResponseEntity<SeatAvailabilityResponse> checkAvailability(
            @PathVariable("id") Long id,
            @RequestParam("quantity") Integer quantity
    ) {


        return ResponseEntity.ok(
                showtimeService.checkAvailability(
                        id,
                        quantity
                )
        );

    }


    /**
     * Đặt ghế cho suất chiếu
     *
     * PUT /api/showtimes/{id}/reserve
     *
     * Dùng cho Booking Service
     */
    @PutMapping("/{id}/reserve")
    public ResponseEntity<ShowtimeResponse> reserveSeats(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReserveSeatRequest request
    ) {


        ShowtimeResponse response =
                showtimeService.reserveSeats(
                        id,
                        request
                );


        return ResponseEntity.ok(response);

    }


    /**
     * Hoàn ghế khi hủy booking
     *
     * PUT /api/showtimes/{id}/release
     */
    @PutMapping("/{id}/release")
    public ResponseEntity<ShowtimeResponse> releaseSeats(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReleaseSeatRequest request
    ) {


        ShowtimeResponse response =
                showtimeService.releaseSeats(
                        id,
                        request
                );


        return ResponseEntity.ok(response);

    }

    /**
     * Xóa suất chiếu
     *
     * DELETE /api/showtimes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(
            @PathVariable("id") Long id
    ) {


        showtimeService.deleteShowtime(id);


        return ResponseEntity
                .noContent()
                .build();

    }


}