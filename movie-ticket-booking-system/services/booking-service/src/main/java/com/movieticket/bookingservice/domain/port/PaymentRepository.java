package com.movieticket.bookingservice.domain.port;

import com.movieticket.bookingservice.domain.entity.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findById(Long id);
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByTransactionRef(String transactionRef);
}
