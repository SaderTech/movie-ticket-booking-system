package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.api.dto.request.CreateHallRequest;
import com.movieticket.cinemaservice.api.dto.response.HallResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class CreateHallUseCase {

    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;

    @Transactional
    @CacheEvict(value = "halls", allEntries = true)

    public HallResponse execute(CreateHallRequest request) {
        Cinema cinema = cinemaRepository.findById(request.cinemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + request.cinemaId()));

        if (hallRepository.existsByCinema_IdAndNameIgnoreCase(request.cinemaId(), request.name())) {
            throw new BusinessException("Hall name already exists in this cinema: " + request.name());
        }

        Hall hall = new Hall(
                cinema,
                request.name(),
                request.capacity(),
                request.hallType(),
                request.status()
        );

        return HallResponse.from(hallRepository.save(hall));
    }
}