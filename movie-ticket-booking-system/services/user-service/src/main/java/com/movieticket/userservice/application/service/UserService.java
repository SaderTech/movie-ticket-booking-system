package com.movieticket.userservice.application.service;


import com.movieticket.userservice.application.dto.request.CreateUserRequest;
import com.movieticket.userservice.application.dto.request.LoginRequest;
import com.movieticket.userservice.application.dto.request.UpdateUserRequest;
import com.movieticket.userservice.application.dto.response.LoginResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;

public interface UserService {

    UserResponse createUser(
            CreateUserRequest request
    );

    LoginResponse login(
            LoginRequest request
    );

    UserResponse getUserById(
            Long userId
    );

    UserResponse updateUser(
            Long userId,
            UpdateUserRequest request
    );

    void deleteUser(
            Long userId
    );
}