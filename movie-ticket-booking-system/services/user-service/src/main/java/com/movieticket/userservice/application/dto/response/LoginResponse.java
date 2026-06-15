package userservice.application.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private Long userId;

    private String username;

    private String email;
}