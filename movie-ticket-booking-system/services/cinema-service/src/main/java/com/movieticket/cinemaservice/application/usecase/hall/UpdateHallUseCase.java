package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.api.dto.request.UpdateHallRequest;
import com.movieticket.cinemaservice.api.dto.response.HallResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class UpdateHallUseCase {

    private final HallRepository hallRepository;

    @Transactional
    @CacheEvict(value = "halls", allEntries = true)

    public HallResponse execute(Long id, UpdateHallRequest request) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));

        Long cinemaId = hall.getCinema().getId();

        hallRepository.findByCinema_IdAndNameIgnoreCase(cinemaId, request.name())
                .ifPresent(existingHall -> {
                    if (!existingHall.getId().equals(hall.getId())) {
                        throw new BusinessException("Hall name already exists in this cinema: " + request.name());
                    }
                });

        hall.update(
                request.name(),
                request.capacity(),
                request.hallType(),
                request.status()
        );

        return HallResponse.from(hallRepository.save(hall));
    }
}