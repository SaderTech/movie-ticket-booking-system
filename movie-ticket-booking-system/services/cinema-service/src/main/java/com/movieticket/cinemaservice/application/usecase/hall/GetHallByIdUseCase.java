package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.api.dto.response.HallResponse;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class GetHallByIdUseCase {

    private final HallRepository hallRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "halls", key = "#id")

    public HallResponse execute(Long id) {
        Hall hall = hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));

        return HallResponse.from(hall);
    }
}