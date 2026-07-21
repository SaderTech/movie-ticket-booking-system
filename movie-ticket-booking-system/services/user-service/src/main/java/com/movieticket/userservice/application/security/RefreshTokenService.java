package com.movieticket.userservice.application.security;




import com.movieticket.userservice.domain.entity.RefreshToken;
import com.movieticket.userservice.domain.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.UUID;



@Service
@RequiredArgsConstructor
public class RefreshTokenService {


    private final RefreshTokenRepository repository;



    public RefreshToken create(
            Long userId
    ) {


        RefreshToken refreshToken =
                RefreshToken.create(

                        UUID.randomUUID()
                                .toString(),

                        userId,

                        LocalDateTime.now()
                                .plusDays(7)

                );


        return repository.save(refreshToken);

    }



    public RefreshToken verify(
            String token
    ){

        return repository.findByToken(token)

                .orElseThrow(
                        () -> new RuntimeException(
                                "Refresh token invalid"
                        )
                );

    }

}