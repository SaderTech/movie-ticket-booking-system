package com.movieticket.cinemaservice.application.usecase.seat;

import com.movieticket.cinemaservice.application.dto.request.CreateSeatRequest;
import com.movieticket.cinemaservice.application.dto.response.SeatResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
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
import org.springframework.cache.annotation.Caching;
@Service
@RequiredArgsConstructor
public class CreateSeatUseCase {

    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "seats", allEntries = true),
            @CacheEvict(value = "halls", allEntries = true)
    })
    public SeatResponse execute(CreateSeatRequest request) {
        Hall hall = hallRepository.findByIdForUpdate(request.hallId())
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + request.hallId()));

        long currentSeatCount = seatRepository.countByHall_Id(request.hallId());
        if (currentSeatCount >= hall.getCapacity()) {
            throw new BusinessException(
                    "Hall capacity has been reached: " + hall.getCapacity()
            );
        }

        SeatType seatType = seatTypeRepository.findById(request.seatTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat type not found with id: " + request.seatTypeId()));

        String normalizedRow = request.rowName().trim().toUpperCase();
        if (seatRepository.existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(
                request.hallId(),
                normalizedRow,
                request.seatNumber()
        )) {
            throw new BusinessException("Seat already exists in this hall: "
                    + normalizedRow + request.seatNumber());
        }

        Seat seat = new Seat(
                hall,
                seatType,
                normalizedRow,
                request.seatNumber(),
                request.status()
        );

        return SeatResponse.from(seatRepository.save(seat));
    }
}
