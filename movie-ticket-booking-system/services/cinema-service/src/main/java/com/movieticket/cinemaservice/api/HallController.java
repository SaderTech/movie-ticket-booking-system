package com.movieticket.cinemaservice.api;

import com.movieticket.cinemaservice.api.dto.request.CreateHallRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateHallRequest;
import com.movieticket.cinemaservice.api.dto.response.HallResponse;
import com.movieticket.cinemaservice.application.CinemaApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/halls")
@RequiredArgsConstructor
public class HallController {

    private final CinemaApplicationService cinemaApplicationService;

    @PostMapping
    public ResponseEntity<HallResponse> createHall(
            @Valid @RequestBody CreateHallRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cinemaApplicationService.createHall(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallResponse> getHallById(@PathVariable Long id) {
        return ResponseEntity.ok(cinemaApplicationService.getHallById(id));
    }

    @GetMapping
    public ResponseEntity<List<HallResponse>> getHallsByCinemaId(
            @RequestParam Long cinemaId
    ) {
        return ResponseEntity.ok(cinemaApplicationService.getHallsByCinemaId(cinemaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HallResponse> updateHall(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHallRequest request
    ) {
        return ResponseEntity.ok(cinemaApplicationService.updateHall(id, request));
    }
}