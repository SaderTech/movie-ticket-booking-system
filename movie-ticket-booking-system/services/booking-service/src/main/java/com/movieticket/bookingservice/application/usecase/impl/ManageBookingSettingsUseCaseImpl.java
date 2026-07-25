package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingSettingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.usecase.ManageBookingSettingsUseCase;
import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.repository.BookingSettingRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ManageBookingSettingsUseCaseImpl implements ManageBookingSettingsUseCase {
    private final BookingSettingRepository bookingSettingRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BookingSettingResponse> findAll() {
        return bookingSettingRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingSettingResponse findByKey(String key) {
        return toResponse(getRequiredSetting(key));
    }

    @Override
    @Transactional
    public BookingSettingResponse update(String key, String value) {
        BookingSetting setting = getRequiredSetting(key);
        setting.setSettingValue(value);
        BookingSetting updated = bookingSettingRepository.save(setting);
        log.info("Updated booking setting: {} = {}", key, value);
        return toResponse(updated);
    }

    private BookingSetting getRequiredSetting(String key) {
        return bookingSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOKING_NOT_FOUND,
                        "Booking setting not found: " + key));
    }

    private BookingSettingResponse toResponse(BookingSetting setting) {
        return BookingSettingResponse.builder()
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .description(setting.getDescription())
                .build();
    }
}
