package userservice.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private String phoneNumber;

    private String avatarUrl;
}