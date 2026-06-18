package com.movieticket.bookingservice.infrastructure.repository;

import com.movieticket.bookingservice.domain.entity.SagaTransaction;
import com.movieticket.bookingservice.domain.port.SagaTransactionRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSagaTransactionRepository;
import com.movieticket.bookingservice.infrastructure.jpa.SagaTransactionJpaEntity;
import com.movieticket.bookingservice.infrastructure.mapper.SagaTransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SagaTransactionRepositoryAdapter implements SagaTransactionRepository {

    private final JpaSagaTransactionRepository jpaSagaTransactionRepository;

    @Override
    public SagaTransaction save(SagaTransaction sagaTransaction) {
        SagaTransactionJpaEntity jpaEntity = SagaTransactionMapper.toEntity(sagaTransaction);
        SagaTransactionJpaEntity savedEntity = jpaSagaTransactionRepository.save(jpaEntity);
        return SagaTransactionMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SagaTransaction> findById(Long id) {
        return jpaSagaTransactionRepository.findById(id)
                .map(SagaTransactionMapper::toDomain);
    }

    @Override
    public Optional<SagaTransaction> findBySagaId(String sagaId) {
        return jpaSagaTransactionRepository.findBySagaId(sagaId)
                .map(SagaTransactionMapper::toDomain);
    }

    @Override
    public Optional<SagaTransaction> findByBookingId(Long bookingId) {
        return jpaSagaTransactionRepository.findByBookingId(bookingId)
                .map(SagaTransactionMapper::toDomain);
    }
}
