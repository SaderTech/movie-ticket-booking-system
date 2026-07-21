package com.movieticket.cinemaservice.api.controller;

import com.movieticket.cinemaservice.api.dto.request.CreateCinemaRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateCinemaRequest;
import com.movieticket.cinemaservice.api.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.application.usecase.cinema.CreateCinemaUseCase;
import com.movieticket.cinemaservice.application.usecase.cinema.GetAllCinemasUseCase;
import com.movieticket.cinemaservice.application.usecase.cinema.GetCinemaByIdUseCase;
import com.movieticket.cinemaservice.application.usecase.cinema.UpdateCinemaUseCase;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CreateCinemaUseCase createCinemaUseCase;
    private final GetAllCinemasUseCase getAllCinemasUseCase;
    private final GetCinemaByIdUseCase getCinemaByIdUseCase;
    private final UpdateCinemaUseCase updateCinemaUseCase;

    @PostMapping
    public ResponseEntity<CinemaResponse> createCinema(
            @Valid @RequestBody CreateCinemaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createCinemaUseCase.execute(request));
    }

    @GetMapping
    public ResponseEntity<List<CinemaResponse>> getCinemas(
            @RequestParam(name = "status", required = false) CinemaStatus status
    ) {
        return ResponseEntity.ok(getAllCinemasUseCase.execute(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaResponse> getCinemaById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getCinemaByIdUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CinemaResponse> updateCinema(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateCinemaRequest request
    ) {
        return ResponseEntity.ok(updateCinemaUseCase.execute(id, request));
    }
}