package com.movieticket.cinemaservice.application.usecase.cinema;

import com.movieticket.cinemaservice.application.dto.request.UpdateCinemaRequest;
import com.movieticket.cinemaservice.application.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
@RequiredArgsConstructor
public class UpdateCinemaUseCase {

    private final CinemaRepository cinemaRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "cinemas", allEntries = true),
            @CacheEvict(value = "halls", allEntries = true)
    })

    public CinemaResponse execute(Long id, UpdateCinemaRequest request) {
        Cinema cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + id));

        String normalizedName = request.name().trim();
        if (!cinema.getName().equalsIgnoreCase(normalizedName)
                && cinemaRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException("Cinema name already exists: " + normalizedName);
        }

        cinema.update(
                normalizedName,
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
