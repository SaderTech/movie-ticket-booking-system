package com.movieticket.userservice.application.service;

import com.movieticket.userservice.application.dto.request.CreateUserRequest;
import com.movieticket.userservice.application.dto.request.LoginRequest;
import com.movieticket.userservice.application.dto.response.LoginResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;
import com.movieticket.userservice.infrastructure.persistence.entity.UserJpaEntity;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JpaUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse register(CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        UserJpaEntity user = new UserJpaEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .build();
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        UserJpaEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return LoginResponse.builder()
                .accessToken("TEMP_TOKEN")
                .tokenType("Bearer")
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    public LoginResponse refreshToken(String refreshToken) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}