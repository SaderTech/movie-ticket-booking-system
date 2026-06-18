package com.movieticket.bookingservice.infrastructure.repository;

import com.movieticket.bookingservice.domain.entity.Payment;
import com.movieticket.bookingservice.domain.port.PaymentRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaPaymentRepository;
import com.movieticket.bookingservice.infrastructure.jpa.PaymentJpaEntity;
import com.movieticket.bookingservice.infrastructure.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final JpaPaymentRepository jpaPaymentRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity jpaEntity = PaymentMapper.toEntity(payment);
        PaymentJpaEntity savedEntity = jpaPaymentRepository.save(jpaEntity);
        return PaymentMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpaPaymentRepository.findById(id)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByBookingId(Long bookingId) {
        return jpaPaymentRepository.findByBookingId(bookingId)
                .map(PaymentMapper::toDomain);
    }

    @Override
    public Optional<Payment> findByTransactionRef(String transactionRef) {
        return jpaPaymentRepository.findByTransactionRef(transactionRef)
                .map(PaymentMapper::toDomain);
    }
}
