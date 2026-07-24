package com.movieticket.bookingservice.domain.repository;

import com.movieticket.bookingservice.domain.entity.BookingSetting;
import java.util.List;
import java.util.Optional;

public interface BookingSettingRepository {
    List<BookingSetting> findAll();
    Optional<BookingSetting> findBySettingKey(String settingKey);
    BookingSetting save(BookingSetting setting);
}
