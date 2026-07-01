package com.movieticket.bookingservice.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaIdempotencyRecordRepository extends JpaRepository<IdempotencyRecordJpaEntity, Long> {
    Optional<IdempotencyRecordJpaEntity> findByIdempotencyKey(String idempotencyKey);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM IdempotencyRecordJpaEntity r WHERE r.expiresAt < :now")
    void deleteExpired(LocalDateTime now);
}
