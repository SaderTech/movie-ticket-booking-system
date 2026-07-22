package com.movieticket.userservice.config;


import com.movieticket.userservice.application.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;



@Configuration
@RequiredArgsConstructor
public class SecurityConfig {



    private final JwtAuthenticationFilter jwtAuthenticationFilter;



    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {



        return http


                .csrf(csrf ->
                        csrf.disable()
                )


                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )


                .authorizeHttpRequests(auth -> auth



                        // =========================
                        // PUBLIC API
                        // =========================

                        .requestMatchers(

                                "/api/auth/**",

                                "/swagger-ui/**",

                                "/v3/api-docs/**"

                        )

                        .permitAll()



                        // =========================
                        // ADMIN ONLY
                        // =========================

                        .requestMatchers(
                                "/api/admin/**"
                        )

                        .hasRole("ADMIN")



                        // =========================
                        // STAFF + ADMIN
                        // =========================

                        .requestMatchers(
                                "/api/staff/**"
                        )

                        .hasAnyRole(
                                "STAFF",
                                "ADMIN"
                        )



                        // =========================
                        // USER + ABOVE
                        // =========================

                        .requestMatchers(
                                "/api/users/**"
                        )

                        .hasAnyRole(
                                "USER",
                                "STAFF",
                                "ADMIN"
                        )



                        // còn lại yêu cầu login
                        .anyRequest()

                        .authenticated()

                )


                .addFilterBefore(

                        jwtAuthenticationFilter,

                        UsernamePasswordAuthenticationFilter.class

                )


                .build();

    }

}