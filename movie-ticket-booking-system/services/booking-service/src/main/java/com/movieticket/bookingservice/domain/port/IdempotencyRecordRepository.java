package com.movieticket.bookingservice.domain.port;

import com.movieticket.bookingservice.domain.entity.IdempotencyRecord;

import java.time.LocalDateTime;
import java.util.Optional;

public interface IdempotencyRecordRepository {
    IdempotencyRecord save(IdempotencyRecord record);
    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
    void deleteExpired(LocalDateTime now);
}
