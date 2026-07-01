package com.movieticket.bookingservice.infrastructure.repository;

import com.movieticket.bookingservice.domain.entity.IdempotencyRecord;
import com.movieticket.bookingservice.domain.port.IdempotencyRecordRepository;
import com.movieticket.bookingservice.infrastructure.jpa.IdempotencyRecordJpaEntity;
import com.movieticket.bookingservice.infrastructure.jpa.JpaIdempotencyRecordRepository;
import com.movieticket.bookingservice.infrastructure.mapper.IdempotencyRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class IdempotencyRecordRepositoryAdapter implements IdempotencyRecordRepository {

    private final JpaIdempotencyRecordRepository jpaIdempotencyRecordRepository;

    @Override
    public IdempotencyRecord save(IdempotencyRecord record) {
        IdempotencyRecordJpaEntity jpaEntity = IdempotencyRecordMapper.toEntity(record);
        IdempotencyRecordJpaEntity savedEntity = jpaIdempotencyRecordRepository.save(jpaEntity);
        return IdempotencyRecordMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey) {
        return jpaIdempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                .map(IdempotencyRecordMapper::toDomain);
    }

    @Override
    public void deleteExpired(LocalDateTime now) {
        jpaIdempotencyRecordRepository.deleteExpired(now);
    }
}
