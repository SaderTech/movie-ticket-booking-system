package com.movieticket.userservice.application.dto.response;


import lombok.Builder;
import lombok.Value;


@Value
@Builder
public class LoginResponse {


    String accessToken;


    String refreshToken;


    String tokenType;


    Long userId;


    String username;


    String email;


    Long expiresIn;

}