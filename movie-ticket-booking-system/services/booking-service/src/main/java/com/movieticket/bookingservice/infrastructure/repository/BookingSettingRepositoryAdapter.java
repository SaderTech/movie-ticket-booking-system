package com.movieticket.bookingservice.infrastructure.repository;

import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.port.BookingSettingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.BookingSettingJpaEntity;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingSettingRepository;
import com.movieticket.bookingservice.infrastructure.mapper.BookingSettingMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BookingSettingRepositoryAdapter implements BookingSettingRepository {

    private final JpaBookingSettingRepository jpaBookingSettingRepository;

    @Override
    public BookingSetting save(BookingSetting setting) {
        BookingSettingJpaEntity jpaEntity = BookingSettingMapper.toEntity(setting);
        BookingSettingJpaEntity savedEntity = jpaBookingSettingRepository.save(jpaEntity);
        return BookingSettingMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BookingSetting> findBySettingKey(String settingKey) {
        return jpaBookingSettingRepository.findBySettingKey(settingKey)
                .map(BookingSettingMapper::toDomain);
    }

    @Override
    public List<BookingSetting> findAll() {
        return jpaBookingSettingRepository.findAll().stream()
                .map(BookingSettingMapper::toDomain)
                .collect(Collectors.toList());
    }
}
