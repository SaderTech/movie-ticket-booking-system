package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.application.dto.request.UpdateHallRequest;
import com.movieticket.cinemaservice.application.dto.response.HallDetailResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class UpdateHallUseCase {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;

    @Transactional
    @CacheEvict(value = "halls", allEntries = true)

    public HallDetailResponse execute(Long id, UpdateHallRequest request) {
        Hall hall = hallRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));

        Long cinemaId = hall.getCinema().getId();
        String normalizedName = request.name().trim();
        long currentSeatCount = seatRepository.countByHall_Id(id);
        if (request.capacity() < currentSeatCount) {
            throw new BusinessException(
                    "Hall capacity cannot be lower than current seat count: " + currentSeatCount
            );
        }

        hallRepository.findByCinema_IdAndNameIgnoreCase(cinemaId, normalizedName)
                .ifPresent(existingHall -> {
                    if (!existingHall.getId().equals(hall.getId())) {
                        throw new BusinessException("Hall name already exists in this cinema: " + normalizedName);
                    }
                });

        hall.update(
                normalizedName,
                request.capacity(),
                request.hallType(),
                request.status()
        );

        return HallDetailResponse.from(hallRepository.save(hall));
    }
}
