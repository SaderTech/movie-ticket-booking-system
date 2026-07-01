package com.movieticket.userservice.application.service;

import com.movieticket.userservice.application.dto.request.CreateUserRequest;
import com.movieticket.userservice.application.dto.request.LoginRequest;
import com.movieticket.userservice.application.dto.response.LoginResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(CreateUserRequest request);

    LoginResponse login(LoginRequest request);

    LoginResponse refreshToken(String refreshToken);
}