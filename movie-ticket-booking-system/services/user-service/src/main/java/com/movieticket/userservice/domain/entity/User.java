package com.movieticket.userservice.domain.entity;

import com.movieticket.userservice.domain.valueobject.Email;
import com.movieticket.userservice.domain.valueobject.FullName;
import com.movieticket.userservice.domain.valueobject.Password;
import com.movieticket.userservice.domain.valueobject.PhoneNumber;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class User {

    private Long id;

    private String username;

    private Email email;

    private Password password;

    private FullName fullName;

    private PhoneNumber phone;

    private String avatar;

    private LocalDate dateOfBirth;

    private String gender;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private User() {}

    // =========================
    // CREATE (business - register)
    // =========================
    public static User create(
            String username,
            Email email,
            Password password,
            FullName fullName,
            PhoneNumber phone,
            String avatar,
            LocalDate dateOfBirth,
            String gender
    ) {

        Objects.requireNonNull(username);
        Objects.requireNonNull(email);
        Objects.requireNonNull(password);
        Objects.requireNonNull(fullName);

        User user = new User();

        user.username = username.trim();
        user.email = email;
        user.password = password;
        user.fullName = fullName;
        user.phone = phone;
        user.avatar = avatar;
        user.dateOfBirth = dateOfBirth;
        user.gender = gender;

        user.active = true;

        user.createdAt = LocalDateTime.now();
        user.updatedAt = LocalDateTime.now();

        return user;
    }

    // =========================
    // RESTORE (DB → Domain)
    // =========================
    public static User restore(
            Long id,
            String username,
            Email email,
            Password password,
            FullName fullName,
            PhoneNumber phone,
            String avatar,
            LocalDate dateOfBirth,
            String gender,
            Boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {

        User user = new User();

        user.id = id;
        user.username = username;
        user.email = email;
        user.password = password;
        user.fullName = fullName;
        user.phone = phone;
        user.avatar = avatar;
        user.dateOfBirth = dateOfBirth;
        user.gender = gender;

        user.active = active;

        user.createdAt = createdAt;
        user.updatedAt = updatedAt;

        return user;
    }

    // =========================
    // BUSINESS METHODS
    // =========================
    public void updateProfile(
            FullName fullName,
            PhoneNumber phone,
            String avatar
    ) {
        this.fullName = fullName;
        this.phone = phone;
        this.avatar = avatar;
        this.updatedAt = LocalDateTime.now();
    }

    public void changePassword(Password password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
}