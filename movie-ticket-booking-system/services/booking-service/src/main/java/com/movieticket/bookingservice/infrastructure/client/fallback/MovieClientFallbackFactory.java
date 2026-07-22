package com.movieticket.bookingservice.infrastructure.client.fallback;

import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.client.dto.MovieResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MovieClientFallbackFactory implements FallbackFactory<MovieClient> {

    @Override
    public MovieClient create(Throwable cause) {
        if (cause instanceof FeignException) {
            log.error("Feign error calling movie-service: status={}, message={}",
                    ((FeignException) cause).status(), cause.getMessage());
        } else {
            log.error("Circuit breaker opened for movie-service: {}", cause.getMessage());
        }
        return new MovieClient() {
            @Override
            public String callMovieDemo() {
                log.warn("Fallback for callMovieDemo");
                return "[Fallback] Movie service unavailable";
            }

            @Override
            public MovieResponse getMovie(Long id) {
                log.warn("Fallback: returning null for movie id={}", id);
                return null;
            }
        };
    }
}
