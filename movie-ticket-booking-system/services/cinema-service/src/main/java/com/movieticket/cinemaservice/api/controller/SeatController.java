package com.movieticket.cinemaservice.api.controller;

import com.movieticket.cinemaservice.application.dto.request.CreateSeatRequest;
import com.movieticket.cinemaservice.application.dto.request.UpdateSeatRequest;
import com.movieticket.cinemaservice.application.dto.response.SeatResponse;
import com.movieticket.cinemaservice.application.usecase.seat.CreateSeatUseCase;
import com.movieticket.cinemaservice.application.usecase.seat.GetSeatByIdUseCase;
import com.movieticket.cinemaservice.application.usecase.seat.GetSeatsByHallIdUseCase;
import com.movieticket.cinemaservice.application.usecase.seat.UpdateSeatUseCase;
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

    private final CreateSeatUseCase createSeatUseCase;
    private final GetSeatByIdUseCase getSeatByIdUseCase;
    private final GetSeatsByHallIdUseCase getSeatsByHallIdUseCase;
    private final UpdateSeatUseCase updateSeatUseCase;

    @PostMapping
    public ResponseEntity<SeatResponse> createSeat(
            @Valid @RequestBody CreateSeatRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createSeatUseCase.execute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatResponse> getSeatById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getSeatByIdUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<List<SeatResponse>> getSeatsByHallId(
            @RequestParam(name = "hallId") Long hallId
    ) {
        return ResponseEntity.ok(getSeatsByHallIdUseCase.execute(hallId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatResponse> updateSeat(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateSeatRequest request
    ) {
        return ResponseEntity.ok(updateSeatUseCase.execute(id, request));
    }
}