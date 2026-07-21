package com.movieticket.userservice.application.service;


import com.movieticket.userservice.application.dto.request.CreateUserRequest;
import com.movieticket.userservice.application.dto.request.LoginRequest;
import com.movieticket.userservice.application.dto.response.LoginResponse;
import com.movieticket.userservice.application.dto.response.UserResponse;

import com.movieticket.userservice.application.security.JwtService;
import com.movieticket.userservice.application.security.RefreshTokenService;

import com.movieticket.userservice.domain.entity.RefreshToken;

import com.movieticket.userservice.exception.BadRequestException;
import com.movieticket.userservice.exception.ResourceNotFoundException;

import com.movieticket.userservice.infrastructure.persistence.entity.UserJpaEntity;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRepository;
import com.movieticket.userservice.infrastructure.persistence.repository.JpaUserRoleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.List;



@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {



    private final JpaUserRepository userRepository;

    private final JpaUserRoleRepository userRoleRepository;


    private final PasswordEncoder passwordEncoder;


    private final JwtService jwtService;


    private final RefreshTokenService refreshTokenService;



    // =====================================================
    // REGISTER
    // =====================================================

    @Override
    public UserResponse register(
            CreateUserRequest request
    ) {


        if (userRepository.existsByEmail(request.getEmail())) {

            throw new BadRequestException(
                    "Email already exists"
            );

        }


        if (userRepository.existsByUsername(request.getUsername())) {

            throw new BadRequestException(
                    "Username already exists"
            );

        }



        UserJpaEntity user =
                new UserJpaEntity();



        user.setUsername(
                request.getUsername()
        );


        user.setEmail(
                request.getEmail()
        );


        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );


        user.setFullName(
                request.getFullName()
        );


        user.setPhone(
                request.getPhone()
        );


        user.setIsActive(true);



        user.setCreatedAt(
                LocalDateTime.now()
        );


        user.setUpdatedAt(
                LocalDateTime.now()
        );



        userRepository.save(user);



        return UserResponse.builder()

                .id(user.getId())

                .username(user.getUsername())

                .email(user.getEmail())

                .fullName(user.getFullName())

                .phone(user.getPhone())

                .avatar(user.getAvatar())

                .build();

    }





    // =====================================================
    // LOGIN
    // =====================================================


    @Override
    public LoginResponse login(
            LoginRequest request
    ) {



        UserJpaEntity user =

                userRepository.findByEmail(
                                request.getEmail()
                        )

                        .orElseThrow(

                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )

                        );





        if (!passwordEncoder.matches(

                request.getPassword(),

                user.getPassword()

        )) {


            throw new BadRequestException(
                    "Invalid password"
            );

        }





        // ============================
        // CREATE ACCESS TOKEN
        // ============================


        String accessToken =

                jwtService.generateToken(

                        user.getId(),
                        user.getEmail(),
                        getRoleNames(user.getId())

                );





        // ============================
        // CREATE REFRESH TOKEN
        // ============================


        String refreshToken =

                refreshTokenService

                        .create(
                                user.getId()
                        )

                        .getToken();







        return LoginResponse.builder()


                .accessToken(accessToken)


                .refreshToken(refreshToken)


                .tokenType("Bearer")


                .userId(user.getId())


                .username(user.getUsername())


                .email(user.getEmail())


                .expiresIn(
                        86400000L
                )


                .build();

    }







    // =====================================================
    // REFRESH TOKEN
    // =====================================================


    @Override
    public LoginResponse refreshToken(
            String refreshToken
    ) {



        RefreshToken token =

                refreshTokenService.verify(
                        refreshToken
                );





        UserJpaEntity user =

                userRepository.findById(
                                token.getUserId()
                        )

                        .orElseThrow(

                                () -> new ResourceNotFoundException(
                                        "User not found"
                                )

                        );






        String newAccessToken =


                jwtService.generateToken(

                        user.getId(),
                        user.getEmail(),
                        getRoleNames(user.getId())

                );







        return LoginResponse.builder()


                .accessToken(newAccessToken)


                .refreshToken(refreshToken)


                .tokenType("Bearer")


                .userId(user.getId())


                .username(user.getUsername())


                .email(user.getEmail())


                .expiresIn(
                        86400000L
                )


                .build();

    }

    private List<String> getRoleNames(Long userId) {
        List<String> roles = userRoleRepository.findRoleNamesByUserId(userId);
        return roles.isEmpty() ? List.of("USER") : roles;
    }


}
