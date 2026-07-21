package com.movieticket.userservice.application.service;

import com.movieticket.userservice.infrastructure.persistence.entity.UserJpaEntity;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.movieticket.userservice.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.movieticket.userservice.application.dto.response.PageResponse;
import com.movieticket.userservice.application.dto.request.UpdateUserRequest;
import com.movieticket.userservice.application.dto.response.UserResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {


    private final JpaUserRepository userRepository;


    @Override
    public UserResponse getUserById(Long userId) {

        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return mapToResponse(user);
    }


    // =====================================
    // PAGINATION + SEARCH
    // =====================================
    @Override
    public PageResponse<UserResponse> getAllUsers(
            String keyword,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);


        Page<UserJpaEntity> userPage;


        // Không nhập keyword -> lấy tất cả
        if (keyword == null || keyword.trim().isEmpty()) {

            userPage = userRepository.findAll(pageable);

        }
        // Có keyword -> search
        else {

            userPage =
                    userRepository
                            .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                                    keyword,
                                    keyword,
                                    keyword,
                                    pageable
                            );
        }


        List<UserResponse> users =
                userPage.getContent()
                        .stream()
                        .map(this::mapToResponse)
                        .collect(Collectors.toList());


        return PageResponse.<UserResponse>builder()
                .content(users)
                .page(userPage.getNumber())
                .size(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
    }



    @Override
    public UserResponse updateUser(
            Long userId,
            UpdateUserRequest request
    ) {

        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));


        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }


        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }


        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }


        user.setUpdatedAt(LocalDateTime.now());


        return mapToResponse(
                userRepository.save(user)
        );
    }


    @Override
    public UserResponse getUserByEmail(String email){

        UserJpaEntity user =
                userRepository.findByEmail(email)
                        .orElseThrow(
                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )
                        );


        return mapToResponse(user);
    }



    @Override
    public void deleteUser(Long userId) {

        UserJpaEntity user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        userRepository.delete(user);
    }



    private UserResponse mapToResponse(UserJpaEntity user) {

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .avatar(user.getAvatar())
                .build();
    }

}