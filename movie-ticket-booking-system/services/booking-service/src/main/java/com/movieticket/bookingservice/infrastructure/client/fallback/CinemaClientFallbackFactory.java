package com.movieticket.bookingservice.infrastructure.client.fallback;

import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.dto.CinemaResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CinemaClientFallbackFactory implements FallbackFactory<CinemaClient> {

    @Override
    public CinemaClient create(Throwable cause) {
        if (cause instanceof FeignException) {
            log.error("Feign error calling cinema-service: status={}, message={}",
                    ((FeignException) cause).status(), cause.getMessage());
        } else {
            log.error("Circuit breaker opened for cinema-service: {}", cause.getMessage());
        }
        return id -> {
            log.warn("Fallback: returning null for cinema id={}", id);
            return null;
        };
    }
}
