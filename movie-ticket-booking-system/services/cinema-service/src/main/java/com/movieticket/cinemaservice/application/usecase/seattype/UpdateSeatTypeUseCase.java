package com.movieticket.cinemaservice.application.usecase.seattype;

import com.movieticket.cinemaservice.api.dto.request.UpdateSeatTypeRequest;
import com.movieticket.cinemaservice.api.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class UpdateSeatTypeUseCase {

    private final SeatTypeRepository seatTypeRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "seat-types", key = "#id"),
            @CacheEvict(value = "seat-types", key = "'all'")
    })
    public SeatTypeResponse execute(Long id, UpdateSeatTypeRequest request) {
        SeatType seatType = seatTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat type not found with id: " + id));

        if (!seatType.getCode().equalsIgnoreCase(request.code())
                && seatTypeRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("Seat type code already exists: " + request.code());
        }

        seatType.update(
                request.code(),
                request.name(),
                request.description()
        );

        return SeatTypeResponse.from(seatTypeRepository.save(seatType));
    }
}