package com.movieticket.userservice.application.security;


import com.movieticket.userservice.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;


@Service
@RequiredArgsConstructor
public class JwtService {


    private final JwtConfig jwtConfig;



    // =========================
    // GENERATE TOKEN
    // =========================

    public String generateToken(
            Long userId,
            String email
    ){

        return Jwts.builder()

                .setSubject(email)

                .claim(
                        "userId",
                        userId
                )

                .setIssuedAt(
                        new Date()
                )

                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        +
                                        jwtConfig.getExpiration()
                        )
                )

                .signWith(
                        Keys.hmacShaKeyFor(
                                jwtConfig.getSecret()
                                        .getBytes(StandardCharsets.UTF_8)
                        ),
                        SignatureAlgorithm.HS256
                )

                .compact();

    }




    // =========================
    // EXTRACT EMAIL
    // =========================

    public String extractEmail(
            String token
    ){

        return getClaims(token)
                .getSubject();

    }




    // =========================
    // EXTRACT USER ID
    // =========================

    public Long extractUserId(
            String token
    ){

        return getClaims(token)
                .get("userId", Long.class);

    }




    // =========================
    // VALIDATE TOKEN
    // =========================

    public boolean validateToken(
            String token
    ){

        try {

            getClaims(token);

            return true;

        }
        catch (ExpiredJwtException e){

            System.out.println("JWT expired");

        }
        catch (JwtException e){

            System.out.println("JWT invalid");

        }

        return false;

    }





    // =========================
    // GET CLAIMS
    // =========================

    private Claims getClaims(
            String token
    ){

        return Jwts.parserBuilder()

                .setSigningKey(
                        jwtConfig.getSecret()
                                .getBytes(StandardCharsets.UTF_8)
                )

                .build()

                .parseClaimsJws(token)

                .getBody();

    }


}