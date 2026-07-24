package com.movieticket.bookingservice.infrastructure.persistence;

import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.repository.BookingSettingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingSettingRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BookingSettingRepositoryAdapter implements BookingSettingRepository {
    private final JpaBookingSettingRepository jpaRepository;
    public List<BookingSetting> findAll() { return jpaRepository.findAll(); }
    public Optional<BookingSetting> findBySettingKey(String key) { return jpaRepository.findBySettingKey(key); }
    public BookingSetting save(BookingSetting setting) { return jpaRepository.save(setting); }
}
