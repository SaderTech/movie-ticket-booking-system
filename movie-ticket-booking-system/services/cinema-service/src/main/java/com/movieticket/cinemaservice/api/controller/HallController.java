package com.movieticket.cinemaservice.api.controller;

import com.movieticket.cinemaservice.application.dto.request.CreateHallRequest;
import com.movieticket.cinemaservice.application.dto.request.UpdateHallRequest;
import com.movieticket.cinemaservice.application.dto.response.HallDetailResponse;
import com.movieticket.cinemaservice.application.dto.response.HallAvailabilityResponse;
import com.movieticket.cinemaservice.application.dto.response.HallSummaryResponse;
import com.movieticket.cinemaservice.application.usecase.hall.CheckHallAvailabilityUseCase;
import com.movieticket.cinemaservice.application.usecase.hall.CreateHallUseCase;
import com.movieticket.cinemaservice.application.usecase.hall.GetHallByIdUseCase;
import com.movieticket.cinemaservice.application.usecase.hall.GetHallsByCinemaIdUseCase;
import com.movieticket.cinemaservice.application.usecase.hall.UpdateHallUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final CreateHallUseCase createHallUseCase;
    private final GetHallByIdUseCase getHallByIdUseCase;
    private final GetHallsByCinemaIdUseCase getHallsByCinemaIdUseCase;
    private final UpdateHallUseCase updateHallUseCase;
    private final CheckHallAvailabilityUseCase checkHallAvailabilityUseCase;

    @PostMapping
    public ResponseEntity<HallDetailResponse> createHall(
            @Valid @RequestBody CreateHallRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createHallUseCase.execute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallDetailResponse> getHallById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getHallByIdUseCase.execute(id));
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<HallAvailabilityResponse> checkAvailability(
            @PathVariable("id") Long id,
            @RequestParam("startTime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("endTime")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        return ResponseEntity.ok(checkHallAvailabilityUseCase.execute(id, startTime, endTime));
    }

    @GetMapping
    public ResponseEntity<List<HallSummaryResponse>> getHallsByCinemaId(
            @RequestParam(name = "cinemaId") Long cinemaId
    ) {
        return ResponseEntity.ok(getHallsByCinemaIdUseCase.execute(cinemaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HallDetailResponse> updateHall(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateHallRequest request
    ) {
        return ResponseEntity.ok(updateHallUseCase.execute(id, request));
    }
}
