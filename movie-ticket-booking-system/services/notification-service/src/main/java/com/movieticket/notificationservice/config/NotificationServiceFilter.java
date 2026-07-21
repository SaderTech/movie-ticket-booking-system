package com.movieticket.notificationservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class NotificationServiceFilter extends OncePerRequestFilter {

    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_USER_EMAIL = "X-User-Email";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String userId = request.getHeader(HEADER_USER_ID);
        String userEmail = request.getHeader(HEADER_USER_EMAIL);

        request.setAttribute("userId", userId);
        request.setAttribute("userEmail", userEmail);

        filterChain.doFilter(request, response);
    }
}
