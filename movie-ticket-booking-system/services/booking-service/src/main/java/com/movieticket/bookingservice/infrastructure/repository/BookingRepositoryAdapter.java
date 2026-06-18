package com.movieticket.bookingservice.infrastructure.repository;

import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.port.BookingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.BookingJpaEntity;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingRepository;
import com.movieticket.bookingservice.infrastructure.mapper.BookingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepository {

    private final JpaBookingRepository jpaBookingRepository;

    @Override
    public Booking save(Booking booking) {
        BookingJpaEntity jpaEntity = BookingMapper.toEntity(booking);
        BookingJpaEntity savedEntity = jpaBookingRepository.save(jpaEntity);
        return BookingMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return jpaBookingRepository.findById(id)
                .map(BookingMapper::toDomain);
    }

    @Override
    public Optional<Booking> findByBookingCode(String bookingCode) {
        return jpaBookingRepository.findByBookingCode(bookingCode)
                .map(BookingMapper::toDomain);
    }

    @Override
    public List<Booking> findByUserId(Long userId) {
        return jpaBookingRepository.findByUserId(userId).stream()
                .map(BookingMapper::toDomain)
                .collect(Collectors.toList());
    }
}
