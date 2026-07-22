package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.entity.BookingSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaBookingSettingRepository extends JpaRepository<BookingSetting, Long> {
    Optional<BookingSetting> findBySettingKey(String settingKey);
}