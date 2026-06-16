package com.movieticket.cinemaservice.api;

import com.movieticket.cinemaservice.api.dto.request.CreateCinemaRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateCinemaRequest;
import com.movieticket.cinemaservice.api.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.application.CinemaApplicationService;
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

    private final CinemaApplicationService cinemaApplicationService;

    @PostMapping
    public ResponseEntity<CinemaResponse> createCinema(
            @Valid @RequestBody CreateCinemaRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cinemaApplicationService.createCinema(request));
    }

    @GetMapping
    public ResponseEntity<List<CinemaResponse>> getCinemas(
            @RequestParam(required = false) CinemaStatus status
    ) {
        return ResponseEntity.ok(cinemaApplicationService.getAllCinemas(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CinemaResponse> getCinemaById(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaApplicationService.getCinemaById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CinemaResponse> updateCinema(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCinemaRequest request
    ) {
        return ResponseEntity.ok(cinemaApplicationService.updateCinema(id, request));
    }
}