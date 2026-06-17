package com.movieticket.bookingservice.domain.port;

import com.movieticket.bookingservice.domain.entity.BookingSetting;

import java.util.List;
import java.util.Optional;

public interface BookingSettingRepository {
    BookingSetting save(BookingSetting setting);
    Optional<BookingSetting> findBySettingKey(String settingKey);
    List<BookingSetting> findAll();
}
