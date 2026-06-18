package com.movieticket.bookingservice.infrastructure.mapper;

import com.movieticket.bookingservice.domain.entity.IdempotencyRecord;
import com.movieticket.bookingservice.infrastructure.jpa.IdempotencyRecordJpaEntity;

public class IdempotencyRecordMapper {

    public static IdempotencyRecord toDomain(IdempotencyRecordJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return IdempotencyRecord.builder()
                .id(entity.getId())
                .idempotencyKey(entity.getIdempotencyKey())
                .requestHash(entity.getRequestHash())
                .operationType(entity.getOperationType())
                .status(entity.getStatus())
                .responseBody(entity.getResponseBody())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    public static IdempotencyRecordJpaEntity toEntity(IdempotencyRecord domain) {
        if (domain == null) {
            return null;
        }
        IdempotencyRecordJpaEntity entity = new IdempotencyRecordJpaEntity();
        entity.setId(domain.getId());
        entity.setIdempotencyKey(domain.getIdempotencyKey());
        entity.setRequestHash(domain.getRequestHash());
        entity.setOperationType(domain.getOperationType());
        entity.setStatus(domain.getStatus());
        entity.setResponseBody(domain.getResponseBody());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        return entity;
    }
}
