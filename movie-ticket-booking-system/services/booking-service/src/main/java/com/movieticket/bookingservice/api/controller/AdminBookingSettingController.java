package com.movieticket.bookingservice.api.controller;

import com.movieticket.bookingservice.api.dto.ApiResponse;
import com.movieticket.bookingservice.api.dto.BookingSettingResponse;
import com.movieticket.bookingservice.api.dto.BookingSettingUpdateRequest;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.port.BookingSettingRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/booking-settings")
@RequiredArgsConstructor
@Slf4j
public class AdminBookingSettingController {

    private final BookingSettingRepository bookingSettingRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingSettingResponse>>> getAllSettings() {
        List<BookingSetting> settings = bookingSettingRepository.findAll();
        List<BookingSettingResponse> response = settings.stream()
                .map(s -> BookingSettingResponse.builder()
                        .settingKey(s.getSettingKey())
                        .settingValue(s.getSettingValue())
                        .description(s.getDescription())
                        .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<BookingSettingResponse>> getSetting(@PathVariable String key) {
        BookingSetting setting = bookingSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOKING_NOT_FOUND,
                        "Booking setting not found: " + key));
        BookingSettingResponse response = BookingSettingResponse.builder()
                .settingKey(setting.getSettingKey())
                .settingValue(setting.getSettingValue())
                .description(setting.getDescription())
                .build();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<BookingSettingResponse>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody BookingSettingUpdateRequest request) {
        BookingSetting setting = bookingSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ApiException(ErrorCode.BOOKING_NOT_FOUND,
                        "Booking setting not found: " + key));
        BookingSetting updated = BookingSetting.builder()
                .id(setting.getId())
                .settingKey(setting.getSettingKey())
                .settingValue(request.getSettingValue())
                .description(setting.getDescription())
                .build();
        updated = bookingSettingRepository.save(updated);
        log.info("Updated booking setting: {} = {}", key, request.getSettingValue());
        BookingSettingResponse response = BookingSettingResponse.builder()
                .settingKey(updated.getSettingKey())
                .settingValue(updated.getSettingValue())
                .description(updated.getDescription())
                .build();
        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully", response));
    }
}
