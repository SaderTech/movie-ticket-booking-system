package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.application.dto.response.HallAvailabilityResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckHallAvailabilityUseCase {

    private final HallRepository hallRepository;
    private final CinemaRepository cinemaRepository;
    private final HallMaintenanceRepository hallMaintenanceRepository;

    @Transactional(readOnly = true)
    public HallAvailabilityResponse execute(Long hallId, LocalDateTime startTime, LocalDateTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException("Availability start time must be before end time");
        }

        Hall hall = hallRepository.findById(hallId)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + hallId));

        Long cinemaId = hall.getCinema().getId();
        Cinema cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + cinemaId));

        if (cinema.getStatus() != CinemaStatus.ACTIVE) {
            return HallAvailabilityResponse.unavailable("Cinema is not active");
        }
        if (hall.getStatus() != HallStatus.ACTIVE) {
            return HallAvailabilityResponse.unavailable("Hall is not active");
        }

        boolean hasMaintenance = !hallMaintenanceRepository.findOverlappingMaintenances(
                hallId,
                startTime,
                endTime,
                MaintenanceStatus.CANCELLED
        ).isEmpty();

        if (hasMaintenance) {
            return HallAvailabilityResponse.unavailable("Hall has an overlapping maintenance schedule");
        }

        return HallAvailabilityResponse.availableResult();
    }
}
