package com.movieticket.movieservice.application.dto.response;

import java.time.LocalDate;

public record PersonResponse(
        Long id,
        String name,
        String avatarUrl,
        String biography,
        LocalDate birthDate,
        String roleName
) {
}
