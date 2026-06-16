package userservice.domain.entity;

import userservice.domain.valueobject.Email;
import userservice.domain.valueobject.Password;

import java.time.LocalDateTime;
import java.util.Objects;

public class User {

    private Long id;

    private String username;

    private Email email;

    private Password password;

    private LocalDateTime createdAt;

    private User() {}

    public static User create(
            String username,
            Email email,
            Password password
    ) {

        Objects.requireNonNull(username);
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);

        User user = new User();

        user.username = username.trim();
        user.email = email;
        user.password = password;
        user.createdAt = LocalDateTime.now();

        return user;
    }

    public void changePassword(Password newPassword) {
        this.password = newPassword;
    }

    public void changeEmail(Email newEmail) {
        this.email = newEmail;
    }
}