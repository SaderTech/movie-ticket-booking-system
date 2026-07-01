package com.movieticket.userservice.domain.entity;


import com.movieticket.userservice.domain.valueobject.FullName;
import com.movieticket.userservice.domain.valueobject.PhoneNumber;

public class UserProfile {

    private Long id;

    private Long userId;

    private FullName fullName;

    private PhoneNumber phoneNumber;

    private String avatarUrl;

    private UserProfile(){}

    public static UserProfile create(
            Long userId,
            FullName fullName,
            PhoneNumber phoneNumber
    ){

        UserProfile profile = new UserProfile();

        profile.userId = userId;
        profile.fullName = fullName;
        profile.phoneNumber = phoneNumber;

        return profile;
    }

    public void updateFullName(FullName fullName){
        this.fullName = fullName;
    }

    public void updatePhoneNumber(PhoneNumber phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public void updateAvatar(String avatarUrl){
        this.avatarUrl = avatarUrl;
    }
}