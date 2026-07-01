package com.movieticket.userservice.application.dto.request;

import lombok.Data;

@Data
public class CreateUserRequest {

    private String username;

    private String email;

    private String password;

    private String fullName;

    private String phoneNumber;
}