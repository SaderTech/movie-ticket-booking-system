package com.movieticket.userservice.infrastructure.config;

import com.movieticket.userservice.domain.repository.UserRepository;
import com.movieticket.userservice.infrastructure.persistence.adapter.UserRepositoryAdapter;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RepositoryConfig {

    @Bean
    public UserRepository userRepository(JpaUserRepository jpaUserRepository) {
        return new UserRepositoryAdapter(jpaUserRepository);
    }
}