package com.movieticket.userservice.application.security;


import com.movieticket.userservice.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;


@Service
@RequiredArgsConstructor
public class JwtService {


    private final JwtConfig jwtConfig;



    // =========================
    // GENERATE TOKEN
    // =========================

    public String generateToken(
            Long userId,
            String email,
            List<String> roles
    ){

        return Jwts.builder()

                .setSubject(email)

                .claim(
                        "userId",
                        userId
                )

                .claim("roles", roles)

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
                        signingKey()
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

        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtConfig.getSecret()));
    }


}
