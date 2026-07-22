package com.movieticket.userservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Entity
@Table(name = "user_roles")
@IdClass(UserRoleJpaEntity.UserRoleId.class)
public class UserRoleJpaEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Getter
    @Setter
    public static class UserRoleId implements Serializable {

        private Long userId;
        private Long roleId;

        public UserRoleId() {
        }
    }
}