package com.movieticket.cinemaservice.application.usecase.maintenance;

import com.movieticket.cinemaservice.api.dto.request.UpdateMaintenanceStatusRequest;
import com.movieticket.cinemaservice.api.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.HallMaintenance;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class UpdateMaintenanceStatusUseCase {

    private final HallMaintenanceRepository hallMaintenanceRepository;

    @Transactional
    @CacheEvict(value = "maintenances", allEntries = true)

    public HallMaintenanceResponse execute(Long id, UpdateMaintenanceStatusRequest request) {
        HallMaintenance maintenance = hallMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found with id: " + id));

        maintenance.changeStatus(request.status());

        return HallMaintenanceResponse.from(hallMaintenanceRepository.save(maintenance));
    }
}