package com.movieticket.bookingservice.infrastructure.client.fallback;

import com.movieticket.bookingservice.infrastructure.client.SeatClient;
import com.movieticket.bookingservice.infrastructure.client.dto.SeatResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SeatClientFallbackFactory implements FallbackFactory<SeatClient> {

    @Override
    public SeatClient create(Throwable cause) {
        if (cause instanceof FeignException) {
            log.error("Feign error calling cinema-service (seats): status={}, message={}",
                    ((FeignException) cause).status(), cause.getMessage());
        } else {
            log.error("Circuit breaker opened for cinema-service (seats): {}", cause.getMessage());
        }
        return hallId -> {
            log.warn("Fallback: returning empty seat list for hallId={}", hallId);
            return List.of();
        };
    }
}
