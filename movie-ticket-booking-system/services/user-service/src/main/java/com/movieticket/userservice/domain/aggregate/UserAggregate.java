package userservice.domain.aggregate;

import userservice.domain.entity.Role;
import userservice.domain.entity.User;
import userservice.domain.entity.UserProfile;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class UserAggregate {

    private final User user;

    private UserProfile profile;

    private final List<Role> roles;

    private UserAggregate(
            User user,
            UserProfile profile,
            List<Role> roles
    ) {
        this.user = user;
        this.profile = profile;
        this.roles = roles;
    }

    public static UserAggregate create(
            User user,
            UserProfile profile
    ) {

        Objects.requireNonNull(user);

        return new UserAggregate(
                user,
                profile,
                new ArrayList<>()
        );
    }

    public void updateProfile(UserProfile profile) {
        this.profile = profile;
    }

    public void assignRole(Role role) {

        Objects.requireNonNull(role);

        boolean exists = roles.stream()
                .anyMatch(r -> r.getName()
                        .equals(role.getName()));

        if (!exists) {
            roles.add(role);
        }
    }

    public void removeRole(String roleName) {

        roles.removeIf(role ->
                role.getName().equals(roleName));
    }
}