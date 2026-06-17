package com.movieticket.bookingservice.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.Map;

@Component
@Order(1)
@Slf4j
public class JwtAuthFilter implements Filter {

    private final SecretKey secretKey;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(@Value("${app.jwt.secret:}") String secret,
                         ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        if (secret != null && !secret.isBlank()) {
            this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } else {
            this.secretKey = null;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (secretKey == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);

            Claims claims = jws.getPayload();
            Object userIdObj = claims.get("userId");
            Long userId = null;
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).longValue();
            }
            httpRequest.setAttribute("userId", userId);
            httpRequest.setAttribute("username", claims.getSubject());
            httpRequest.setAttribute("roles", claims.get("roles"));
            chain.doFilter(request, response);
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json");
            objectMapper.writeValue(httpResponse.getOutputStream(),
                    Map.of("success", false, "message", "Invalid or expired token"));
        }
    }
}