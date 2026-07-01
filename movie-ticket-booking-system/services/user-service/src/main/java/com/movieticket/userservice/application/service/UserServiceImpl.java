package com.movieticket.userservice.application.service;

import com.movieticket.userservice.application.dto.request.CreateUserRequest;
import com.movieticket.userservice.application.dto.request.LoginRequest;
import com.movieticket.userservice.application.dto.request.UpdateUserRequest;
import com.movieticket.userservice.application.dto.response.LoginResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Override
    public UserResponse createUser(
            CreateUserRequest request
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet");
    }

    @Override
    public LoginResponse login(
            LoginRequest request
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet");
    }

    @Override
    public UserResponse getUserById(
            Long userId
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet");
    }

    @Override
    public UserResponse updateUser(
            Long userId,
            UpdateUserRequest request
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet");
    }

    @Override
    public void deleteUser(
            Long userId
    ) {
        throw new UnsupportedOperationException(
                "Not implemented yet");
    }
}