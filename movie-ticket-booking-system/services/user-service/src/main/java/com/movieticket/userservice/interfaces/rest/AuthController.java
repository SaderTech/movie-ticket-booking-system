package com.movieticket.userservice.interfaces.rest;

import com.movieticket.userservice.application.dto.request.CreateUserRequest;
import com.movieticket.userservice.application.dto.request.LoginRequest;
import com.movieticket.userservice.application.dto.response.LoginResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;
import com.movieticket.userservice.application.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final AuthService authService;

    // =========================
    // REGISTER
    // =========================
    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @Valid @RequestBody CreateUserRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    // =========================
    // LOGIN
    // =========================
    @Operation(summary = "User login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    // =========================
    // REFRESH TOKEN
    // =========================
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @RequestParam(name = "refreshToken") String refreshToken) {

        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}