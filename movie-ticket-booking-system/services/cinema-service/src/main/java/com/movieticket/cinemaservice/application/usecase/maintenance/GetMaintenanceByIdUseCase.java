package com.movieticket.cinemaservice.application.usecase.maintenance;

import com.movieticket.cinemaservice.application.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.HallMaintenance;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetMaintenanceByIdUseCase {

    private final HallMaintenanceRepository hallMaintenanceRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "maintenances", key = "#p0")

    public HallMaintenanceResponse execute(Long id) {
        HallMaintenance maintenance = hallMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found with id: " + id));

        return HallMaintenanceResponse.from(maintenance);
    }
}