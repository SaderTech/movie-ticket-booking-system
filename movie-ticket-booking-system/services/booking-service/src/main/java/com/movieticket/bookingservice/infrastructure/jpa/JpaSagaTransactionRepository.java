package com.movieticket.bookingservice.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaSagaTransactionRepository extends JpaRepository<SagaTransactionJpaEntity, Long> {
    Optional<SagaTransactionJpaEntity> findBySagaId(String sagaId);
    Optional<SagaTransactionJpaEntity> findByBookingId(Long bookingId);
}
