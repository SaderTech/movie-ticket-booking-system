package com.movieticket.cinemaservice.application.usecase.cinema;

import com.movieticket.cinemaservice.application.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
public class GetCinemaByIdUseCase {

    private final CinemaRepository cinemaRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "cinemas", key = "#p0")

    public CinemaResponse execute(Long id) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + id));

        return CinemaResponse.from(cinema);
    }
}