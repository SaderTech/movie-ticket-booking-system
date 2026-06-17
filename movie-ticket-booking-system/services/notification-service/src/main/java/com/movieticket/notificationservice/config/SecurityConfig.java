package com.movieticket.notificationservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final NotificationServiceFilter notificationServiceFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/ping",
                                "/actuator/**",
                                "/api/v1/qr-codes/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .addFilterBefore(notificationServiceFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}