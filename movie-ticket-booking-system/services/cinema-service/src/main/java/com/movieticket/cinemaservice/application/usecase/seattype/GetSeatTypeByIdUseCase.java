package com.movieticket.cinemaservice.application.usecase.seattype;

import com.movieticket.cinemaservice.api.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSeatTypeByIdUseCase {

    private final SeatTypeRepository seatTypeRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "seat-types", key = "#id")
    public SeatTypeResponse execute(Long id) {
        SeatType seatType = seatTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat type not found with id: " + id));

        return SeatTypeResponse.from(seatType);
    }
}