package com.movieticket.cinemaservice.application.usecase.cinema;

import com.movieticket.cinemaservice.api.dto.request.UpdateCinemaRequest;
import com.movieticket.cinemaservice.api.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

@Service
@RequiredArgsConstructor
public class UpdateCinemaUseCase {

    private final CinemaRepository cinemaRepository;

    @Transactional
    @CacheEvict(value = "cinemas", allEntries = true)

    public CinemaResponse execute(Long id, UpdateCinemaRequest request) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + id));

        if (!cinema.getName().equalsIgnoreCase(request.name())
                && cinemaRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Cinema name already exists: " + request.name());
        }

        cinema.update(
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