package com.movieticket.bookingservice.infrastructure.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

//@Component
public class FeignClientConfig {

    @Bean
    public RequestInterceptor headerPropagationInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                // Nhặt thông tin từ Request hiện tại của Booking Service
                String correlationId = request.getHeader("X-Correlation-ID");
                String userId = request.getHeader("X-User-ID");
                String userEmail = request.getHeader("X-User-Email");

                // Tiến hành lan truyền (Propagation) đính kèm vào Feign để gửi đi tiếp
                if (correlationId != null) requestTemplate.header("X-Correlation-ID", correlationId);
                if (userId != null) requestTemplate.header("X-User-ID", userId);
                if (userEmail != null) requestTemplate.header("X-User-Email", userEmail);
            }
        };
    }
}
