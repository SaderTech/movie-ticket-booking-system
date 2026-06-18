package com.movieticket.bookingservice.infrastructure.mapper;

import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.infrastructure.jpa.BookingEventOutboxJpaEntity;

public class BookingEventOutboxMapper {

    public static BookingEventOutbox toDomain(BookingEventOutboxJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return BookingEventOutbox.builder()
                .id(entity.getId())
                .eventId(entity.getEventId())
                .aggregateType(entity.getAggregateType())
                .aggregateId(entity.getAggregateId())
                .bookingId(entity.getBookingId())
                .eventType(entity.getEventType())
                .topic(entity.getTopic())
                .payloadJson(entity.getPayloadJson())
                .status(entity.getStatus())
                .retryCount(entity.getRetryCount())
                .lastError(entity.getLastError())
                .createdAt(entity.getCreatedAt())
                .publishedAt(entity.getPublishedAt())
                .build();
    }

    public static BookingEventOutboxJpaEntity toEntity(BookingEventOutbox domain) {
        if (domain == null) {
            return null;
        }
        BookingEventOutboxJpaEntity entity = new BookingEventOutboxJpaEntity();
        entity.setId(domain.getId());
        entity.setEventId(domain.getEventId());
        entity.setAggregateType(domain.getAggregateType());
        entity.setAggregateId(domain.getAggregateId());
        entity.setBookingId(domain.getBookingId());
        entity.setEventType(domain.getEventType());
        entity.setTopic(domain.getTopic());
        entity.setPayloadJson(domain.getPayloadJson());
        entity.setStatus(domain.getStatus());
        entity.setRetryCount(domain.getRetryCount());
        entity.setLastError(domain.getLastError());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setPublishedAt(domain.getPublishedAt());
        return entity;
    }
}
