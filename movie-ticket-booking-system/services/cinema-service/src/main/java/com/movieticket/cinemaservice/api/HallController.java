package com.movieticket.cinemaservice.api;

import com.movieticket.cinemaservice.api.dto.request.CreateHallRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateHallRequest;
import com.movieticket.cinemaservice.api.dto.response.HallResponse;
import com.movieticket.cinemaservice.application.usecase.hall.CreateHallUseCase;
import com.movieticket.cinemaservice.application.usecase.hall.GetHallByIdUseCase;
import com.movieticket.cinemaservice.application.usecase.hall.GetHallsByCinemaIdUseCase;
import com.movieticket.cinemaservice.application.usecase.hall.UpdateHallUseCase;
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

    private final CreateHallUseCase createHallUseCase;
    private final GetHallByIdUseCase getHallByIdUseCase;
    private final GetHallsByCinemaIdUseCase getHallsByCinemaIdUseCase;
    private final UpdateHallUseCase updateHallUseCase;

    @PostMapping
    public ResponseEntity<HallResponse> createHall(
            @Valid @RequestBody CreateHallRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createHallUseCase.execute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallResponse> getHallById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getHallByIdUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<List<HallResponse>> getHallsByCinemaId(
            @RequestParam(name = "cinemaId") Long cinemaId
    ) {
        return ResponseEntity.ok(getHallsByCinemaIdUseCase.execute(cinemaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HallResponse> updateHall(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateHallRequest request
    ) {
        return ResponseEntity.ok(updateHallUseCase.execute(id, request));
    }
}