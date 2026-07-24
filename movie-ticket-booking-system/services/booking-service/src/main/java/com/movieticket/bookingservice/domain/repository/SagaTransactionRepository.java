package com.movieticket.bookingservice.domain.repository;

import com.movieticket.bookingservice.domain.entity.SagaTransaction;
import java.util.Optional;

public interface SagaTransactionRepository {
    SagaTransaction save(SagaTransaction saga);
    Optional<SagaTransaction> findByBookingId(Long bookingId);
}
