package com.movieticket.cinemaservice.application.usecase.seat;

import com.movieticket.cinemaservice.api.dto.request.UpdateSeatRequest;
import com.movieticket.cinemaservice.api.dto.response.SeatResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Seat;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class UpdateSeatUseCase {

    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;

    @Transactional
    @CacheEvict(value = "seats", allEntries = true)

    public SeatResponse execute(Long id, UpdateSeatRequest request) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));

        SeatType seatType = seatTypeRepository.findById(request.seatTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat type not found with id: " + request.seatTypeId()));

        Long hallId = seat.getHall().getId();

        seatRepository.findByHall_IdAndRowNameIgnoreCaseAndSeatNumber(
                hallId,
                request.rowName(),
                request.seatNumber()
        ).ifPresent(existingSeat -> {
            if (!existingSeat.getId().equals(seat.getId())) {
                throw new BusinessException("Seat already exists in this hall: "
                        + request.rowName() + request.seatNumber());
            }
        });

        seat.update(
                seatType,
                request.rowName(),
                request.seatNumber(),
                request.status()
        );

        return SeatResponse.from(seatRepository.save(seat));
    }
}