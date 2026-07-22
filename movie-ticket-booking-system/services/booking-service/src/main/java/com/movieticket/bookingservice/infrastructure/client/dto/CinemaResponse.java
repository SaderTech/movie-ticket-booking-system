package com.movieticket.bookingservice.infrastructure.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CinemaResponse(
        Long id,
        String name,
        String address,
        String city,
        String contactPhone,
        BigDecimal latitude,
        BigDecimal longitude,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
