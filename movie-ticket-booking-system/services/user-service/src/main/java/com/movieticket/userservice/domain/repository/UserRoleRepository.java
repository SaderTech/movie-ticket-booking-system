package com.movieticket.userservice.domain.repository;

import com.movieticket.userservice.domain.entity.UserRole;

import java.util.List;

public interface UserRoleRepository {

    UserRole save(UserRole userRole);

    List<UserRole> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void delete(Long userId, Long roleId);
}