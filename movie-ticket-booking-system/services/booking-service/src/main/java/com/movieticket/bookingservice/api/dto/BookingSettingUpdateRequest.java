package com.movieticket.bookingservice.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingSettingUpdateRequest {
    @NotBlank(message = "settingValue is required")
    private String settingValue;
}
