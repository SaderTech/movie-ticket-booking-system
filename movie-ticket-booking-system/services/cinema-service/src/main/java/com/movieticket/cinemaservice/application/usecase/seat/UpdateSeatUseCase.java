package com.movieticket.cinemaservice.application.usecase.seat;

import com.movieticket.cinemaservice.application.dto.request.UpdateSeatRequest;
import com.movieticket.cinemaservice.application.dto.response.SeatResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Seat;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
@Service
@RequiredArgsConstructor
public class UpdateSeatUseCase {

    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "seats", allEntries = true),
            @CacheEvict(value = "halls", allEntries = true)
    })

    public SeatResponse execute(Long id, UpdateSeatRequest request) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));

        SeatType seatType = seatTypeRepository.findById(request.seatTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat type not found with id: " + request.seatTypeId()));

        Long hallId = seat.getHall().getId();
        String normalizedRow = request.rowName().trim().toUpperCase();

        seatRepository.findByHall_IdAndRowNameIgnoreCaseAndSeatNumber(
                hallId,
                normalizedRow,
                request.seatNumber()
        ).ifPresent(existingSeat -> {
            if (!existingSeat.getId().equals(seat.getId())) {
                throw new BusinessException("Seat already exists in this hall: "
                        + normalizedRow + request.seatNumber());
            }
        });

        seat.update(
                seatType,
                normalizedRow,
                request.seatNumber(),
                request.status()
        );

        return SeatResponse.from(seatRepository.save(seat));
    }
}
