package com.movieticket.cinemaservice.application.usecase.seat;

import com.movieticket.cinemaservice.api.dto.response.SeatResponse;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Seat;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class GetSeatByIdUseCase {

    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "seats", key = "#id")
    public SeatResponse execute(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));

        return SeatResponse.from(seat);
    }
}