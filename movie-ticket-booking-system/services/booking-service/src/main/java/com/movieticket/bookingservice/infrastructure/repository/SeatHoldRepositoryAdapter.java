package com.movieticket.bookingservice.infrastructure.repository;

import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import com.movieticket.bookingservice.domain.port.SeatHoldRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSeatHoldRepository;
import com.movieticket.bookingservice.infrastructure.jpa.SeatHoldJpaEntity;
import com.movieticket.bookingservice.infrastructure.mapper.SeatHoldMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SeatHoldRepositoryAdapter implements SeatHoldRepository {

    private final JpaSeatHoldRepository jpaSeatHoldRepository;

    @Override
    public SeatHold save(SeatHold seatHold) {
        SeatHoldJpaEntity jpaEntity = SeatHoldMapper.toEntity(seatHold);
        SeatHoldJpaEntity savedEntity = jpaSeatHoldRepository.save(jpaEntity);
        return SeatHoldMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SeatHold> findById(Long id) {
        return jpaSeatHoldRepository.findById(id)
                .map(SeatHoldMapper::toDomain);
    }

    @Override
    public Optional<SeatHold> findByHoldToken(String holdToken) {
        return jpaSeatHoldRepository.findByHoldToken(holdToken)
                .map(SeatHoldMapper::toDomain);
    }

    @Override
    public List<SeatHold> findExpiredHolds(LocalDateTime now) {
        return jpaSeatHoldRepository.findByStatusAndExpiresAtBefore(SeatHoldStatus.ACTIVE, now).stream()
                .map(SeatHoldMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsActiveHoldForSeat(Long showtimeId, String seatCode, LocalDateTime now) {
        return jpaSeatHoldRepository.existsActiveHoldForSeat(showtimeId, seatCode, SeatHoldStatus.ACTIVE, now);
    }
}
