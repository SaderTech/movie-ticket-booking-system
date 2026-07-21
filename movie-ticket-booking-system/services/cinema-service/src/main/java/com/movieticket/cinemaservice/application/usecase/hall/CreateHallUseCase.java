package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.application.dto.request.CreateHallRequest;
import com.movieticket.cinemaservice.application.dto.response.HallDetailResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
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

    public HallDetailResponse execute(CreateHallRequest request) {
        Cinema cinema = cinemaRepository.findById(request.cinemaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + request.cinemaId()));

        String normalizedName = request.name().trim();
        if (hallRepository.existsByCinema_IdAndNameIgnoreCase(request.cinemaId(), normalizedName)) {
            throw new BusinessException("Hall name already exists in this cinema: " + normalizedName);
        }

        Hall hall = new Hall(
                cinema,
                normalizedName,
                request.capacity(),
                request.hallType(),
                request.status()
        );

        return HallDetailResponse.from(hallRepository.save(hall));
    }
}
