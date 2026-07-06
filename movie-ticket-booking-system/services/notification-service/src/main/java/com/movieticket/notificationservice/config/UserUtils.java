package com.movieticket.notificationservice.config;

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

    public String getCurrentUserEmail(HttpServletRequest request) {
        Object userEmail = request.getAttribute("userEmail");

        if (userEmail == null) {
            return "UNKNOWN";
        }

        return userEmail.toString();
    }
}
