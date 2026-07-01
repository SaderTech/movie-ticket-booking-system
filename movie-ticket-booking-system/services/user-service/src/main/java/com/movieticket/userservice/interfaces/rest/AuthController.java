package com.movieticket.userservice.interfaces.rest;

import com.movieticket.userservice.application.service.AuthService;
import com.movieticket.userservice.application.dto.request.CreateUserRequest;
import com.movieticket.userservice.application.dto.request.LoginRequest;
import com.movieticket.userservice.application.dto.response.LoginResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // =========================
    // REGISTER
    // =========================
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // =========================
    // LOGIN
    // =========================
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // =========================
    // REFRESH TOKEN
    // =========================
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestParam String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}