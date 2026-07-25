package com.movieticket.bookingservice.infrastructure.persistence;

import com.movieticket.bookingservice.domain.entity.Payment;
import com.movieticket.bookingservice.domain.repository.PaymentRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaPaymentRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {
    private final JpaPaymentRepository jpaRepository;
    public Payment save(Payment payment) { return jpaRepository.save(payment); }
    public Optional<Payment> findByBookingId(Long bookingId) { return jpaRepository.findByBookingId(bookingId); }
    public Optional<Payment> findByTransactionRef(String transactionRef) { return jpaRepository.findByTransactionRef(transactionRef); }
}
