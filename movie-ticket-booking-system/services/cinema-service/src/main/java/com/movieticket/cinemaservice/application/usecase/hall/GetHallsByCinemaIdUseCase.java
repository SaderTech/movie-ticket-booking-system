package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.api.dto.response.HallResponse;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetHallsByCinemaIdUseCase {

    private final HallRepository hallRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "halls", key = "'cinema:' + #p0")

    public List<HallResponse> execute(Long cinemaId) {
        return hallRepository.findByCinema_Id(cinemaId)
                .stream()
                .map(HallResponse::from)
                .toList();
    }
}