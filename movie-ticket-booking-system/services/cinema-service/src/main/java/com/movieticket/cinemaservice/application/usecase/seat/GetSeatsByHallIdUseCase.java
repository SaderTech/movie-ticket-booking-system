package com.movieticket.cinemaservice.application.usecase.seat;

import com.movieticket.cinemaservice.api.dto.response.SeatResponse;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
@Service
@RequiredArgsConstructor
public class GetSeatsByHallIdUseCase {

    private final SeatRepository seatRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "seats", key = "'hall:' + #p0")

    public List<SeatResponse> execute(Long hallId) {
        return seatRepository.findByHall_IdOrderByRowNameAscSeatNumberAsc(hallId)
                .stream()
                .map(SeatResponse::from)
                .toList();
    }
}