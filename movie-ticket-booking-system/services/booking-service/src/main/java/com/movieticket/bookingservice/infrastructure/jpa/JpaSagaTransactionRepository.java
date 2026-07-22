package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.entity.SagaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaSagaTransactionRepository extends JpaRepository<SagaTransaction, Long> {
    Optional<SagaTransaction> findBySagaId(String sagaId);
    Optional<SagaTransaction> findByBookingId(Long bookingId);
}