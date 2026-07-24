package com.movieticket.bookingservice.infrastructure.persistence;

import com.movieticket.bookingservice.domain.entity.SagaTransaction;
import com.movieticket.bookingservice.domain.repository.SagaTransactionRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSagaTransactionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SagaTransactionRepositoryAdapter implements SagaTransactionRepository {
    private final JpaSagaTransactionRepository jpaRepository;
    public SagaTransaction save(SagaTransaction saga) { return jpaRepository.save(saga); }
    public Optional<SagaTransaction> findByBookingId(Long bookingId) { return jpaRepository.findByBookingId(bookingId); }
}
