package com.movieticket.cinemaservice.application.usecase.seattype;

import com.movieticket.cinemaservice.api.dto.request.CreateSeatTypeRequest;
import com.movieticket.cinemaservice.api.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class CreateSeatTypeUseCase {

    private final SeatTypeRepository seatTypeRepository;

    @Transactional
    @CacheEvict(value = "seat-types", allEntries = true)

    public SeatTypeResponse execute(CreateSeatTypeRequest request) {
        if (seatTypeRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("Seat type code already exists: " + request.code());
        }

        SeatType seatType = new SeatType(
                request.code(),
                request.name(),
                request.description()
        );

        return SeatTypeResponse.from(seatTypeRepository.save(seatType));
    }
}