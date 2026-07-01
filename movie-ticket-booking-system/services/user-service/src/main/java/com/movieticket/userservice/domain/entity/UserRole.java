package com.movieticket.userservice.domain.entity;

public class UserRole {

    private Long userId;

    private Long roleId;

    private UserRole(){}

    public static UserRole create(
            Long userId,
            Long roleId
    ){

        UserRole userRole = new UserRole();

        userRole.userId = userId;
        userRole.roleId = roleId;

        return userRole;
    }
}