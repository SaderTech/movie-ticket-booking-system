package userservice.application.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequest {

    private String fullName;

    private String phoneNumber;

    private String avatarUrl;
}