package com.movieticket.notificationservice.infrastructure.client.dto;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        String avatar
) {
}
