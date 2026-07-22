package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.application.dto.response.HallSummaryResponse;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
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
    private final CinemaRepository cinemaRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "halls", key = "'cinema:' + #p0")

    public List<HallSummaryResponse> execute(Long cinemaId) {
        if (!cinemaRepository.existsById(cinemaId)) {
            throw new ResourceNotFoundException("Cinema not found with id: " + cinemaId);
        }
        return hallRepository.findByCinema_Id(cinemaId)
                .stream()
                .map(HallSummaryResponse::from)
                .toList();
    }
}
