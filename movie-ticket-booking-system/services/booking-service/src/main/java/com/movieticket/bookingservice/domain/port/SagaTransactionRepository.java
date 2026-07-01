package com.movieticket.bookingservice.domain.port;

import com.movieticket.bookingservice.domain.entity.SagaTransaction;

import java.util.Optional;

public interface SagaTransactionRepository {
    SagaTransaction save(SagaTransaction sagaTransaction);
    Optional<SagaTransaction> findById(Long id);
    Optional<SagaTransaction> findBySagaId(String sagaId);
    Optional<SagaTransaction> findByBookingId(Long bookingId);
}
