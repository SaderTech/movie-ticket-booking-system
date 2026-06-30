package com.movieticket.cinemaservice.application.usecase.maintenance;

import com.movieticket.cinemaservice.api.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class GetMaintenancesByHallIdUseCase {

    private final HallMaintenanceRepository hallMaintenanceRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "maintenances", key = "'hall:' + #hallId")

    public List<HallMaintenanceResponse> execute(Long hallId) {
        return hallMaintenanceRepository.findByHall_IdOrderByStartTimeDesc(hallId)
                .stream()
                .map(HallMaintenanceResponse::from)
                .toList();
    }
}