package com.movieticket.cinemaservice.application.usecase.seat;

import com.movieticket.cinemaservice.application.dto.response.SeatResponse;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class GetSeatsByHallIdUseCase {

    private final SeatRepository seatRepository;
    private final HallRepository hallRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "seats", key = "'hall:' + #p0")

    public List<SeatResponse> execute(Long hallId) {
        if (!hallRepository.existsById(hallId)) {
            throw new ResourceNotFoundException("Hall not found with id: " + hallId);
        }
        return seatRepository.findByHall_IdOrderByRowNameAscSeatNumberAsc(hallId)
                .stream()
                .map(SeatResponse::from)
                .toList();
    }
}
