package com.movieticket.userservice.domain.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RefreshToken {

    private Long id;

    private String token;

    private Long userId;

    private LocalDateTime expiryDate;

    private Boolean revoked;

    private LocalDateTime createdAt;

    private RefreshToken() {
    }

    public static RefreshToken create(
            String token,
            Long userId,
            LocalDateTime expiryDate
    ) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.token = token;
        refreshToken.userId = userId;
        refreshToken.expiryDate = expiryDate;
        refreshToken.revoked = false;
        refreshToken.createdAt = LocalDateTime.now();

        return refreshToken;
    }

    public void revoke() {
        this.revoked = true;
    }
}