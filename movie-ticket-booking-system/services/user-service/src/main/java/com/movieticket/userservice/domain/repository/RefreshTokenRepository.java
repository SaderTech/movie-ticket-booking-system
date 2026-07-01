package com.movieticket.userservice.domain.repository;

import com.movieticket.userservice.domain.entity.RefreshToken;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByToken(String token);
}