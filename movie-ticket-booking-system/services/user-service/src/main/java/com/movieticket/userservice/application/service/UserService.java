package userservice.application.service;

import userservice.application.dto.request.CreateUserRequest;
import userservice.application.dto.request.LoginRequest;
import userservice.application.dto.request.UpdateUserRequest;
import userservice.application.dto.response.LoginResponse;
import userservice.application.dto.response.UserResponse;

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