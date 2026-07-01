package com.movieticket.userservice.domain.repository;

import com.movieticket.userservice.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void deleteById(Long id);
}