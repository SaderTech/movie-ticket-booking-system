package com.movieticket.cinemaservice.application.usecase.seat;

import com.movieticket.cinemaservice.api.dto.request.CreateSeatRequest;
import com.movieticket.cinemaservice.api.dto.response.SeatResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.aggregate.hall.Seat;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class CreateSeatUseCase {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;

    @Transactional
    @CacheEvict(value = "seats", allEntries = true)
    public SeatResponse execute(CreateSeatRequest request) {
        Hall hall = hallRepository.findById(request.hallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + request.hallId()));

        SeatType seatType = seatTypeRepository.findById(request.seatTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat type not found with id: " + request.seatTypeId()));

        if (seatRepository.existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(
                request.hallId(),
                request.rowName(),
                request.seatNumber()
        )) {
            throw new BusinessException("Seat already exists in this hall: "
                    + request.rowName() + request.seatNumber());
        }

        Seat seat = new Seat(
                hall,
                seatType,
                request.rowName(),
                request.seatNumber(),
                request.status()
        );

        return SeatResponse.from(seatRepository.save(seat));
    }
}