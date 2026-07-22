package com.movieticket.bookingservice.infrastructure.client.dto;

import java.time.LocalDate;

public record PersonResponse(
        Long id,
        String name,
        String avatarUrl,
        String biography,
        LocalDate birthDate,
        String roleName
) {}
