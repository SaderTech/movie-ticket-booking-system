package com.movieticket.bookingservice.infrastructure.config;

import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class FeignClientConfig {

    @Bean
    public RequestInterceptor headerPropagationInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attributes == null) {
                log.warn("=> [Feign Interceptor] Không lấy được request hiện tại");
                return;
            }

            HttpServletRequest request = attributes.getRequest();

            String correlationId = request.getHeader("X-Correlation-ID");
            String userId = request.getHeader("X-User-ID");
            String userEmail = request.getHeader("X-User-Email");

            log.info("============== FEIGN INTERCEPTOR ==============");
            log.info("=> Chuẩn bị truyền header sang Movie Service");
            log.info("=> Correlation ID: {}", correlationId);
            log.info("=> User ID: {}", userId);
            log.info("=> User Email: {}", userEmail);
            log.info("===============================================");

            if (correlationId != null) {
                requestTemplate.header("X-Correlation-ID", correlationId);
            }

            if (userId != null) {
                requestTemplate.header("X-User-ID", userId);
            }

            if (userEmail != null) {
                requestTemplate.header("X-User-Email", userEmail);
            }
        };
    }
}