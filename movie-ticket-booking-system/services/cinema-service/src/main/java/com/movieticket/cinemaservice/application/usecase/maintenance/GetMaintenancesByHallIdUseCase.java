package com.movieticket.cinemaservice.application.usecase.maintenance;

import com.movieticket.cinemaservice.application.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class GetMaintenancesByHallIdUseCase {

    private final HallMaintenanceRepository hallMaintenanceRepository;
    private final HallRepository hallRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "maintenances", key = "'hall:' + #p0")

    public List<HallMaintenanceResponse> execute(Long hallId) {
        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException("Hall not found with id: " + hallId);
        }
        return hallMaintenanceRepository.findByHall_IdOrderByStartTimeDesc(hallId)
                .stream()
                .map(HallMaintenanceResponse::from)
                .toList();
    }
}
