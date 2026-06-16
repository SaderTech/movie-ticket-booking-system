package userservice.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import userservice.application.dto.request.CreateUserRequest;
import userservice.application.dto.request.LoginRequest;
import userservice.application.dto.request.UpdateUserRequest;
import userservice.application.dto.response.LoginResponse;
import userservice.application.dto.response.UserResponse;

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