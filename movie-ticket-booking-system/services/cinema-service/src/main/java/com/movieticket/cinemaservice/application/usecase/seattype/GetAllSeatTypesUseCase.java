package com.movieticket.cinemaservice.application.usecase.seattype;

import com.movieticket.cinemaservice.api.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetAllSeatTypesUseCase {

    private final SeatTypeRepository seatTypeRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "seat-types", key = "'all'")

    public List<SeatTypeResponse> execute() {
        return seatTypeRepository.findAll()
                .stream()
                .map(SeatTypeResponse::from)
                .toList();
    }
}