package com.movieticket.userservice.application.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private String phone;

    private String avatar;
}