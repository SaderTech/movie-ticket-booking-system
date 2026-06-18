package com.movieticket.bookingservice.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<PaymentJpaEntity, Long> {
    Optional<PaymentJpaEntity> findByBookingId(Long bookingId);
    Optional<PaymentJpaEntity> findByTransactionRef(String transactionRef);
}
