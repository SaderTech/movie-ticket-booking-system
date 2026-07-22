package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByTransactionRef(String transactionRef);
}