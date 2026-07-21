package com.movieticket.cinemaservice.api.controller;

import com.movieticket.cinemaservice.api.dto.request.CreateSeatTypeRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateSeatTypeRequest;
import com.movieticket.cinemaservice.api.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.application.usecase.seattype.CreateSeatTypeUseCase;
import com.movieticket.cinemaservice.application.usecase.seattype.GetAllSeatTypesUseCase;
import com.movieticket.cinemaservice.application.usecase.seattype.GetSeatTypeByIdUseCase;
import com.movieticket.cinemaservice.application.usecase.seattype.UpdateSeatTypeUseCase;
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

    private final CreateSeatTypeUseCase createSeatTypeUseCase;
    private final GetAllSeatTypesUseCase getAllSeatTypesUseCase;
    private final GetSeatTypeByIdUseCase getSeatTypeByIdUseCase;
    private final UpdateSeatTypeUseCase updateSeatTypeUseCase;

    @PostMapping
    public ResponseEntity<SeatTypeResponse> createSeatType(
            @Valid @RequestBody CreateSeatTypeRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createSeatTypeUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<SeatTypeResponse>> getSeatTypes() {
        return ResponseEntity.ok(getAllSeatTypesUseCase.execute());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeatTypeResponse> getSeatTypeById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getSeatTypeByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeatTypeResponse> updateSeatType(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateSeatTypeRequest request
    ) {
        return ResponseEntity.ok(updateSeatTypeUseCase.execute(id, request));
    }
}