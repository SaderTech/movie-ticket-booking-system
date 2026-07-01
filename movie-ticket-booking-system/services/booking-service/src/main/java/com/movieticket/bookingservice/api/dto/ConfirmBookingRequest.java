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
public class ConfirmBookingRequest {
    @NotBlank(message = "holdToken is required")
    private String holdToken;

    @NotBlank(message = "paymentMethod is required")
    private String paymentMethod;

    private String returnUrl;
}
