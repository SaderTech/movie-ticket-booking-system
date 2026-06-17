package com.movieticket.notificationservice.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {

    public String getCurrentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");

        if (userId == null) {
            return "system";
        }

        return userId.toString();
    }

    public String getCurrentUserRole(HttpServletRequest request) {
        Object userRole = request.getAttribute("userRole");

        if (userRole == null) {
            return "UNKNOWN";
        }

        return userRole.toString();
    }
}