package com.movieticket.userservice.application.service;



import com.movieticket.userservice.application.dto.request.UpdateUserRequest;
import com.movieticket.userservice.application.dto.response.PageResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;


public interface UserService {


    UserResponse getUserById(Long userId);


    // ==========================
    // PAGINATION + SEARCH
    // ==========================
    PageResponse<UserResponse> getAllUsers(
            String keyword,
            int page,
            int size
    );


    UserResponse updateUser(
            Long userId,
            UpdateUserRequest request
    );


    void deleteUser(Long userId);

    UserResponse getUserByEmail(String email);

}