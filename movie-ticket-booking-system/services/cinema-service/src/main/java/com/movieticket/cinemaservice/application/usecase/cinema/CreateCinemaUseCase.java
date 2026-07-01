package com.movieticket.cinemaservice.application.usecase.cinema;

import com.movieticket.cinemaservice.api.dto.request.CreateCinemaRequest;
import com.movieticket.cinemaservice.api.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateCinemaUseCase {

    private final CinemaRepository cinemaRepository;

    @Transactional
    @CacheEvict(value = "cinemas", allEntries = true)
    public CinemaResponse execute(CreateCinemaRequest request) {
        if (cinemaRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Cinema name already exists: " + request.name());
        }

        Cinema cinema = new Cinema(
                request.name(),
                request.address(),
                request.city(),
                request.contactPhone(),
                request.latitude(),
                request.longitude(),
                request.status()
        );

        return CinemaResponse.from(cinemaRepository.save(cinema));
    }
}