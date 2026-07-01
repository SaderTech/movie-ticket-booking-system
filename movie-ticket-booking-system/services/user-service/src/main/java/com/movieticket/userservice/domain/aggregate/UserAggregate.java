package com.movieticket.userservice.domain.aggregate;

import com.movieticket.userservice.domain.entity.RefreshToken;
import com.movieticket.userservice.domain.entity.Role;
import com.movieticket.userservice.domain.entity.User;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class UserAggregate {

    private final User user;

    private final List<Role> roles;

    private final List<RefreshToken> refreshTokens;

    private UserAggregate(
            User user,
            List<Role> roles,
            List<RefreshToken> refreshTokens
    ) {
        this.user = user;
        this.roles = roles;
        this.refreshTokens = refreshTokens;
    }

    public static UserAggregate create(User user) {

        Objects.requireNonNull(user);

        return new UserAggregate(
                user,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    // ================= ROLE =================

    public void assignRole(Role role) {

        Objects.requireNonNull(role);

        boolean exists = roles.stream()
                .anyMatch(r ->
                        r.getRoleName().equalsIgnoreCase(role.getRoleName()));

        if (!exists) {
            roles.add(role);
        }
    }

    public void removeRole(String roleName) {

        roles.removeIf(role ->
                role.getRoleName().equalsIgnoreCase(roleName));
    }

    public boolean hasRole(String roleName) {

        return roles.stream()
                .anyMatch(role ->
                        role.getRoleName().equalsIgnoreCase(roleName));
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    // ================= REFRESH TOKEN =================

    public void addRefreshToken(RefreshToken token) {

        Objects.requireNonNull(token);

        refreshTokens.add(token);
    }

    public void revokeAllTokens() {

        refreshTokens.forEach(RefreshToken::revoke);
    }
}