package com.movieticket.userservice.domain.entity;

public class Role {

    private Long id;

    private String name;

    private Role(){}

    public static Role create(String name){

        Role role = new Role();

        role.name = name;

        return role;
    }
}