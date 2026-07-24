package com.movieticket.bookingservice.domain.repository;

import com.movieticket.bookingservice.domain.entity.IdempotencyRecord;
import java.util.Optional;

public interface IdempotencyRecordRepository {
    IdempotencyRecord save(IdempotencyRecord record);
    IdempotencyRecord saveAndFlush(IdempotencyRecord record);
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
    void delete(IdempotencyRecord record);
    void flush();
}
