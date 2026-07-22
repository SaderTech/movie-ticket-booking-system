package com.movieticket.cinemaservice.application.usecase.seattype;

import com.movieticket.cinemaservice.application.dto.request.UpdateSeatTypeRequest;
import com.movieticket.cinemaservice.application.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
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
            @CacheEvict(value = "seat-types", allEntries = true),
            @CacheEvict(value = "seats", allEntries = true),
            @CacheEvict(value = "halls", allEntries = true)
    })
    public SeatTypeResponse execute(Long id, UpdateSeatTypeRequest request) {
        SeatType seatType = seatTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat type not found with id: " + id));

        String normalizedCode = request.code().trim().toUpperCase();
        if (!seatType.getCode().equalsIgnoreCase(normalizedCode)
                && seatTypeRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new BusinessException("Seat type code already exists: " + normalizedCode);
        }

        seatType.update(
                normalizedCode,
                request.name(),
                request.description()
        );

        return SeatTypeResponse.from(seatTypeRepository.save(seatType));
    }
}
