package com.movieticket.userservice.application.service;

import com.movieticket.userservice.application.dto.request.UpdateUserRequest;
import com.movieticket.userservice.application.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long userId);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long userId, UpdateUserRequest request);

    void deleteUser(Long userId);
}