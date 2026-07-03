package com.movieticket.bookingservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BookingSetting {
    private Long id;
    private String settingKey;
    private String settingValue;
    private String description;
}
