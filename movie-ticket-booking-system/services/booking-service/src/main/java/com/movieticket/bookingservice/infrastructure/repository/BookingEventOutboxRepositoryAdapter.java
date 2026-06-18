package com.movieticket.bookingservice.infrastructure.repository;

import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.port.BookingEventOutboxRepository;
import com.movieticket.bookingservice.infrastructure.jpa.BookingEventOutboxJpaEntity;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingEventOutboxRepository;
import com.movieticket.bookingservice.infrastructure.mapper.BookingEventOutboxMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookingEventOutboxRepositoryAdapter implements BookingEventOutboxRepository {

    private final JpaBookingEventOutboxRepository jpaBookingEventOutboxRepository;

    @Override
    public BookingEventOutbox save(BookingEventOutbox outbox) {
        BookingEventOutboxJpaEntity jpaEntity = BookingEventOutboxMapper.toEntity(outbox);
        BookingEventOutboxJpaEntity savedEntity = jpaBookingEventOutboxRepository.save(jpaEntity);
        return BookingEventOutboxMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BookingEventOutbox> findById(Long id) {
        return jpaBookingEventOutboxRepository.findById(id)
                .map(BookingEventOutboxMapper::toDomain);
    }

    @Override
    public List<BookingEventOutbox> findPendingEvents(OutboxStatus status, int limit) {
        return jpaBookingEventOutboxRepository.findByStatusOrderByCreatedAtAsc(status, PageRequest.of(0, limit)).stream()
                .map(BookingEventOutboxMapper::toDomain)
                .collect(Collectors.toList());
    }
}
