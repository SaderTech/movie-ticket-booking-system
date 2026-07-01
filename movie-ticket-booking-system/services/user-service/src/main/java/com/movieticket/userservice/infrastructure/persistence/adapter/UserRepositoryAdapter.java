package com.movieticket.userservice.infrastructure.persistence.adapter;

import com.movieticket.userservice.domain.entity.User;
import com.movieticket.userservice.domain.repository.UserRepository;
import com.movieticket.userservice.infrastructure.persistence.entity.UserJpaEntity;
import com.movieticket.userservice.infrastructure.persistence.mapper.UserMapper;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    // =========================
    // SAVE
    // =========================
    @Override
    public User save(User user) {
        UserJpaEntity entity = UserMapper.toJpa(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    // =========================
    // FIND BY ID
    // =========================
    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id)
                .map(UserMapper::toDomain);
    }

    // =========================
    // FIND BY USERNAME
    // =========================
    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username)
                .map(UserMapper::toDomain);
    }

    // =========================
    // FIND BY EMAIL
    // =========================
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(UserMapper::toDomain);
    }

    // =========================
    // FIND ALL
    // =========================
    @Override
    public List<User> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.toList());
    }

    // =========================
    // EXISTS BY USERNAME
    // =========================
    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    // =========================
    // EXISTS BY EMAIL
    // =========================
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    // =========================
    // DELETE
    // =========================
    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}