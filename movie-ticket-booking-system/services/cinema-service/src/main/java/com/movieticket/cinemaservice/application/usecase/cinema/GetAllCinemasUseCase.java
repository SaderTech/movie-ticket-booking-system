package com.movieticket.cinemaservice.application.usecase.cinema;

import com.movieticket.cinemaservice.api.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllCinemasUseCase {

    private final CinemaRepository cinemaRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "cinemas", key = "#p0 == null ? 'all' : #p0.name()")

    public List<CinemaResponse> execute(CinemaStatus status) {
        if (status != null) {
            return cinemaRepository.findByStatus(status)
                    .stream()
                    .map(CinemaResponse::from)
                    .toList();
        }

        return cinemaRepository.findAll()
                .stream()
                .map(CinemaResponse::from)
                .toList();
    }
}