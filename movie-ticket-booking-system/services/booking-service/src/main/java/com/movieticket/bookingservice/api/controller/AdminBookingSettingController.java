package com.movieticket.bookingservice.api.controller;

import com.movieticket.bookingservice.api.dto.ApiResponse;
import com.movieticket.bookingservice.api.dto.BookingSettingResponse;
import com.movieticket.bookingservice.api.dto.BookingSettingUpdateRequest;
import com.movieticket.bookingservice.application.usecase.ManageBookingSettingsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/booking-settings")
@RequiredArgsConstructor
public class AdminBookingSettingController {

    private final ManageBookingSettingsUseCase manageBookingSettingsUseCase;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingSettingResponse>>> getAllSettings() {
        return ResponseEntity.ok(ApiResponse.success(manageBookingSettingsUseCase.findAll()));
    }

    @GetMapping("/{key}")
    public ResponseEntity<ApiResponse<BookingSettingResponse>> getSetting(@PathVariable String key) {
        return ResponseEntity.ok(ApiResponse.success(manageBookingSettingsUseCase.findByKey(key)));
    }

    @PutMapping("/{key}")
    public ResponseEntity<ApiResponse<BookingSettingResponse>> updateSetting(
            @PathVariable String key,
            @Valid @RequestBody BookingSettingUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Setting updated successfully",
                manageBookingSettingsUseCase.update(key, request.getSettingValue())));
    }
}
