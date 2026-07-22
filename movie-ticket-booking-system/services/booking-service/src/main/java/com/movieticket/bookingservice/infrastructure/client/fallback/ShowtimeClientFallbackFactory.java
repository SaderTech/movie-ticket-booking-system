package com.movieticket.bookingservice.infrastructure.client.fallback;

import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import com.movieticket.bookingservice.infrastructure.client.dto.ShowtimeResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ShowtimeClientFallbackFactory implements FallbackFactory<ShowtimeClient> {

    @Override
    public ShowtimeClient create(Throwable cause) {
        if (cause instanceof FeignException) {
            log.error("Feign error calling showtime-service: status={}, message={}",
                    ((FeignException) cause).status(), cause.getMessage());
        } else {
            log.error("Circuit breaker opened for showtime-service: {}", cause.getMessage());
        }
        return id -> {
            log.warn("Fallback: returning null for showtime id={}", id);
            return null;
        };
    }
}
