package com.movieticket.bookingservice.infrastructure.persistence;

import com.movieticket.bookingservice.domain.entity.IdempotencyRecord;
import com.movieticket.bookingservice.domain.repository.IdempotencyRecordRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaIdempotencyRecordRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class IdempotencyRecordRepositoryAdapter implements IdempotencyRecordRepository {
    private final JpaIdempotencyRecordRepository jpaRepository;
    public IdempotencyRecord save(IdempotencyRecord record) { return jpaRepository.save(record); }
    public IdempotencyRecord saveAndFlush(IdempotencyRecord record) { return jpaRepository.saveAndFlush(record); }
    public Optional<IdempotencyRecord> findByIdempotencyKey(String key) { return jpaRepository.findByIdempotencyKey(key); }
    public void delete(IdempotencyRecord record) { jpaRepository.delete(record); }
    public void flush() { jpaRepository.flush(); }
}
