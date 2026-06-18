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

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // Lấy thông tin thật do Gateway đã giải mã và đính vào Header
        String userIdStr = httpRequest.getHeader("X-User-ID");
        String userEmail = httpRequest.getHeader("X-User-Email");

        if (userIdStr != null && !userIdStr.isBlank()) {
            try {
                Long userId = Long.parseLong(userIdStr);
                // Lưu vào Request Context để Controller của Booking lấy ra dùng
                httpRequest.setAttribute("userId", userId);
                httpRequest.setAttribute("username", userEmail);
                log.debug("=> [Booking Filter] Đã thiết lập Context cho UserID: {}", userId);
            } catch (NumberFormatException e) {
                log.warn("=> [Booking Filter] UserID không hợp lệ: {}", userIdStr);
            }
        }

        chain.doFilter(request, response);
    }
}