package com.movieticket.cinemaservice.application.usecase.cinema;

import com.movieticket.cinemaservice.application.dto.request.CreateCinemaRequest;
import com.movieticket.cinemaservice.application.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
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
        String normalizedName = request.name().trim();
        if (cinemaRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new BusinessException("Cinema name already exists: " + normalizedName);
        }

        Cinema cinema = new Cinema(
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
