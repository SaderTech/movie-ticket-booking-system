package com.movieticket.bookingservice.domain.repository;

import com.movieticket.bookingservice.domain.entity.Payment;
import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByBookingId(Long bookingId);
    Optional<Payment> findByTransactionRef(String transactionRef);
}
