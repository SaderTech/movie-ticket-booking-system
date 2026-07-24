package com.movieticket.bookingservice.application.usecase;

import com.movieticket.bookingservice.api.dto.BookingSettingResponse;
import java.util.List;

public interface ManageBookingSettingsUseCase {
    List<BookingSettingResponse> findAll();
    BookingSettingResponse findByKey(String key);
    BookingSettingResponse update(String key, String value);
}
