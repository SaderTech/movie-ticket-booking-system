package com.movieticket.userservice.domain.repository;



import com.movieticket.userservice.domain.entity.UserProfile;

import java.util.Optional;

public interface UserProfileRepository {

    UserProfile save(UserProfile profile);

    Optional<UserProfile> findByUserId(Long userId);
}