package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.application.dto.response.HallDetailResponse;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import org.springframework.cache.annotation.Cacheable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
public class GetHallByIdUseCase {

    private final HallRepository hallRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "halls", key = "#p0")

    public HallDetailResponse execute(Long id) {
        Hall hall = hallRepository.findDetailById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));

        return HallDetailResponse.from(hall);
    }
}
