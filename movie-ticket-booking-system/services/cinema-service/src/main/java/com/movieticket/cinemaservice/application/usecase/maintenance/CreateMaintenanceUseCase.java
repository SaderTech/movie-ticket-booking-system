package com.movieticket.cinemaservice.application.usecase.maintenance;

import com.movieticket.cinemaservice.application.dto.request.CreateHallMaintenanceRequest;
import com.movieticket.cinemaservice.application.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.aggregate.hall.HallMaintenance;
import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateMaintenanceUseCase {

    private final HallRepository hallRepository;
    private final HallMaintenanceRepository hallMaintenanceRepository;

    @Transactional
    @CacheEvict(value = "maintenances", allEntries = true)

    public HallMaintenanceResponse execute(CreateHallMaintenanceRequest request) {
        Hall hall = hallRepository.findByIdForUpdate(request.hallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + request.hallId()));

        if (!request.startTime().isBefore(request.endTime())) {
            throw new BusinessException("Maintenance start time must be before end time");
        }
        if (request.startTime().isBefore(java.time.LocalDateTime.now())) {
            throw new BusinessException("Maintenance start time must not be in the past");
        }

        List<HallMaintenance> overlaps = hallMaintenanceRepository.findOverlappingMaintenances(
                request.hallId(),
                request.startTime(),
                request.endTime(),
                MaintenanceStatus.CANCELLED
        );

        if (!overlaps.isEmpty()) {
            throw new BusinessException("Maintenance time overlaps with existing schedule");
        }

        HallMaintenance maintenance = new HallMaintenance(
                hall,
                request.startTime(),
                request.endTime(),
                request.reason()
        );

        return HallMaintenanceResponse.from(hallMaintenanceRepository.save(maintenance));
    }
}
