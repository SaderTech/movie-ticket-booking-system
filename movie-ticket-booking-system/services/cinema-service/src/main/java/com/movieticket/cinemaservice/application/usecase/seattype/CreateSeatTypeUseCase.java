package com.movieticket.cinemaservice.application.usecase.seattype;

import com.movieticket.cinemaservice.application.dto.request.CreateSeatTypeRequest;
import com.movieticket.cinemaservice.application.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
@Service
@RequiredArgsConstructor
public class CreateSeatTypeUseCase {

    private final SeatTypeRepository seatTypeRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "seat-types", allEntries = true),
            @CacheEvict(value = "seats", allEntries = true),
            @CacheEvict(value = "halls", allEntries = true)
    })

    public SeatTypeResponse execute(CreateSeatTypeRequest request) {
        String normalizedCode = request.code().trim().toUpperCase();
        if (seatTypeRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new BusinessException("Seat type code already exists: " + normalizedCode);
        }

        SeatType seatType = new SeatType(
                normalizedCode,
                request.name(),
                request.description()
        );

        return SeatTypeResponse.from(seatTypeRepository.save(seatType));
    }
}
