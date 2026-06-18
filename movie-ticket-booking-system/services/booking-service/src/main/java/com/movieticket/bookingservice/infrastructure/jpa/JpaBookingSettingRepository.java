package com.movieticket.bookingservice.infrastructure.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JpaBookingSettingRepository extends JpaRepository<BookingSettingJpaEntity, Long> {
    Optional<BookingSettingJpaEntity> findBySettingKey(String settingKey);
}
