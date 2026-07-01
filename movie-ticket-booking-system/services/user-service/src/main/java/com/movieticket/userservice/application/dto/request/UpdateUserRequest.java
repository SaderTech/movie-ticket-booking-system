package com.movieticket.userservice.application.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String fullName;

    private String phone;

    private String avatar;
}