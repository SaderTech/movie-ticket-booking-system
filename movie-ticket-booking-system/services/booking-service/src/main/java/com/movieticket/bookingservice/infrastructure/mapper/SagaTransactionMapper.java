package com.movieticket.bookingservice.infrastructure.mapper;

import com.movieticket.bookingservice.domain.entity.SagaTransaction;
import com.movieticket.bookingservice.infrastructure.jpa.SagaTransactionJpaEntity;

public class SagaTransactionMapper {

    public static SagaTransaction toDomain(SagaTransactionJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return SagaTransaction.builder()
                .id(entity.getId())
                .sagaId(entity.getSagaId())
                .bookingId(entity.getBookingId())
                .status(entity.getStatus())
                .currentStep(entity.getCurrentStep())
                .failureReason(entity.getFailureReason())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SagaTransactionJpaEntity toEntity(SagaTransaction domain) {
        if (domain == null) {
            return null;
        }
        SagaTransactionJpaEntity entity = new SagaTransactionJpaEntity();
        entity.setId(domain.getId());
        entity.setSagaId(domain.getSagaId());
        entity.setBookingId(domain.getBookingId());
        entity.setStatus(domain.getStatus());
        entity.setCurrentStep(domain.getCurrentStep());
        entity.setFailureReason(domain.getFailureReason());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }
}
