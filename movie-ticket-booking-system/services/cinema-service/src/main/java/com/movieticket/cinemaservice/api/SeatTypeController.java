package com.movieticket.cinemaservice.api;

import com.movieticket.cinemaservice.api.dto.request.CreateSeatTypeRequest;
import com.movieticket.cinemaservice.api.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.application.CinemaApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seat-types")
@RequiredArgsConstructor
public class SeatTypeController {

    private final CinemaApplicationService cinemaApplicationService;

    @PostMapping
    public ResponseEntity<SeatTypeResponse> createSeatType(
            @Valid @RequestBody CreateSeatTypeRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cinemaApplicationService.createSeatType(request));
    }

    @GetMapping
    public ResponseEntity<List<SeatTypeResponse>> getSeatTypes() {
        return ResponseEntity.ok(cinemaApplicationService.getAllSeatTypes());
    }
}