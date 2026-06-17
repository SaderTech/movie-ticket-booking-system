package com.movieticket.bookingservice.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class BookingContext {

    public Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        Object userId = request.getAttribute("userId");
        return userId instanceof Long ? (Long) userId : null;
    }

    public String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        Object username = request.getAttribute("username");
        return username instanceof String ? (String) username : null;
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest();
    }
}