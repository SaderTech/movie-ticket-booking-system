package com.movieticket.userservice.domain.repository;

import com.movieticket.userservice.domain.entity.UserRole;

import java.util.List;

public interface UserRoleRepository {

    UserRole save(UserRole userRole);

    List<UserRole> findByUserId(Long userId);

    List<UserRole> findByRoleId(Long roleId);

    boolean existsByUserIdAndRoleId(
            Long userId,
            Long roleId
    );

    List<String> findRoleNamesByUserId(
            Long userId
    );

    void deleteByUserId(Long userId);

    void deleteByUserIdAndRoleId(
            Long userId,
            Long roleId
    );

    void updateRoleId(
            Long oldRoleId,
            Long newRoleId
    );
}