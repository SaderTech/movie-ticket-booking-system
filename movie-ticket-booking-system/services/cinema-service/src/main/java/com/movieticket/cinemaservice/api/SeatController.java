package com.movieticket.cinemaservice.api;

import com.movieticket.cinemaservice.api.dto.request.CreateSeatRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateSeatRequest;
import com.movieticket.cinemaservice.api.dto.response.SeatResponse;
import com.movieticket.cinemaservice.application.CinemaApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final CinemaApplicationService cinemaApplicationService;

    @PostMapping
    public ResponseEntity<SeatResponse> createSeat(
            @Valid @RequestBody CreateSeatRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cinemaApplicationService.createSeat(request));
    }

    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeatsByHallId(
            @RequestParam Long hallId
    ) {
        return ResponseEntity.ok(cinemaApplicationService.getSeatsByHallId(hallId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatResponse> updateSeat(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSeatRequest request
    ) {
        return ResponseEntity.ok(cinemaApplicationService.updateSeat(id, request));
    }
}