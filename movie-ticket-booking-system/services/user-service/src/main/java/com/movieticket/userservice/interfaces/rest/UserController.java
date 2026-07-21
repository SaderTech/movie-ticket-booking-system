package com.movieticket.userservice.interfaces.rest;

import com.movieticket.userservice.application.dto.request.UpdateUserRequest;
import com.movieticket.userservice.application.dto.response.PageResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;
import com.movieticket.userservice.application.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User Management APIs")
public class UserController {

    private final UserService userService;

    // =========================
    // GET USERS
    // PAGINATION + SEARCH
    // =========================
    @GetMapping
    @Operation(summary = "Get users with pagination and search")
    public ResponseEntity<PageResponse<UserResponse>> getAll(

            @RequestParam(
                    name = "keyword",
                    required = false
            )
            String keyword,

            @RequestParam(
                    name = "page",
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    name = "size",
                    defaultValue = "10"
            )
            int size

    ) {

        return ResponseEntity.ok(
                userService.getAllUsers(
                        keyword,
                        page,
                        size
                )
        );

    }

    // =========================
    // GET USER BY ID
    // =========================
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserResponse> getById(
            @PathVariable("id") Long id
    ) {

        return ResponseEntity.ok(
                userService.getUserById(id)
        );

    }

    // =========================
    // UPDATE USER
    // =========================
    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<UserResponse> update(

            @PathVariable("id") Long id,

            @Valid
            @RequestBody UpdateUserRequest request

    ) {

        return ResponseEntity.ok(
                userService.updateUser(
                        id,
                        request
                )
        );

    }

    // =========================
    // DELETE USER
    // =========================
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<Void> delete(

            @PathVariable("id") Long id

    ) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();

    }

    // =========================
    // GET CURRENT USER
    // =========================
    @GetMapping("/me")
    @Operation(summary = "Get current logged in user")
    public ResponseEntity<UserResponse> getMe(
            Authentication authentication
    ) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                userService.getUserByEmail(email)
        );

    }

}