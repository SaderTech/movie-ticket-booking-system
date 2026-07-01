package com.movieticket.cinemaservice.api;

import com.movieticket.cinemaservice.api.dto.request.CreateHallMaintenanceRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateMaintenanceStatusRequest;
import com.movieticket.cinemaservice.api.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.application.usecase.maintenance.CreateMaintenanceUseCase;
import com.movieticket.cinemaservice.application.usecase.maintenance.GetMaintenanceByIdUseCase;
import com.movieticket.cinemaservice.application.usecase.maintenance.GetMaintenancesByHallIdUseCase;
import com.movieticket.cinemaservice.application.usecase.maintenance.UpdateMaintenanceStatusUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hall-maintenances")
@RequiredArgsConstructor
public class HallMaintenanceController {

    private final CreateMaintenanceUseCase createMaintenanceUseCase;
    private final GetMaintenanceByIdUseCase getMaintenanceByIdUseCase;
    private final GetMaintenancesByHallIdUseCase getMaintenancesByHallIdUseCase;
    private final UpdateMaintenanceStatusUseCase updateMaintenanceStatusUseCase;

    @PostMapping
    public ResponseEntity<HallMaintenanceResponse> createMaintenance(
            @Valid @RequestBody CreateHallMaintenanceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createMaintenanceUseCase.execute(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HallMaintenanceResponse> getMaintenanceById(
            @PathVariable("id") Long id
    ) {
        return ResponseEntity.ok(getMaintenanceByIdUseCase.execute(id));
    }

    @GetMapping
    public ResponseEntity<List<HallMaintenanceResponse>> getMaintenancesByHallId(
            @RequestParam(name = "hallId") Long hallId
    ) {
        return ResponseEntity.ok(getMaintenancesByHallIdUseCase.execute(hallId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<HallMaintenanceResponse> updateMaintenanceStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateMaintenanceStatusRequest request
    ) {
        return ResponseEntity.ok(updateMaintenanceStatusUseCase.execute(id, request));
    }
}