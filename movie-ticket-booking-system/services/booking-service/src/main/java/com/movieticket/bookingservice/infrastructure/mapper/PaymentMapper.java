package com.movieticket.bookingservice.infrastructure.mapper;

import com.movieticket.bookingservice.domain.entity.Payment;
import com.movieticket.bookingservice.infrastructure.jpa.PaymentJpaEntity;

public class PaymentMapper {

    public static Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return Payment.builder()
                .id(entity.getId())
                .bookingId(entity.getBookingId())
                .transactionRef(entity.getTransactionRef())
                .method(entity.getMethod())
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .paidAt(entity.getPaidAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .failureReason(entity.getFailureReason())
                .rawResponse(entity.getRawResponse())
                .build();
    }

    public static PaymentJpaEntity toEntity(Payment domain) {
        if (domain == null) {
            return null;
        }
        PaymentJpaEntity entity = new PaymentJpaEntity();
        entity.setId(domain.getId());
        entity.setBookingId(domain.getBookingId());
        entity.setTransactionRef(domain.getTransactionRef());
        entity.setMethod(domain.getMethod());
        entity.setAmount(domain.getAmount());
        entity.setStatus(domain.getStatus());
        entity.setPaidAt(domain.getPaidAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        entity.setFailureReason(domain.getFailureReason());
        entity.setRawResponse(domain.getRawResponse());
        return entity;
    }
}
