package com.movieticket.cinemaservice.api;

import com.movieticket.cinemaservice.api.dto.request.CreateHallMaintenanceRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateMaintenanceStatusRequest;
import com.movieticket.cinemaservice.api.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.application.CinemaApplicationService;
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

    private final CinemaApplicationService cinemaApplicationService;

    @PostMapping
    public ResponseEntity<HallMaintenanceResponse> createMaintenance(
            @Valid @RequestBody CreateHallMaintenanceRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cinemaApplicationService.createMaintenance(request));
    }

    @GetMapping
    public ResponseEntity<List<HallMaintenanceResponse>> getMaintenancesByHallId(
            @RequestParam Long hallId
    ) {
        return ResponseEntity.ok(cinemaApplicationService.getMaintenancesByHallId(hallId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<HallMaintenanceResponse> updateMaintenanceStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMaintenanceStatusRequest request
    ) {
        return ResponseEntity.ok(cinemaApplicationService.updateMaintenanceStatus(id, request));
    }
}