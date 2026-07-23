package com.movieticket.bookingservice.infrastructure.client;

import com.movieticket.bookingservice.infrastructure.client.dto.CinemaResponse;
import com.movieticket.bookingservice.infrastructure.client.fallback.CinemaClientFallbackFactory;
import com.movieticket.bookingservice.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "cinema-service", configuration = FeignClientConfig.class,
        url = "${CINEMA_SERVICE_URL:http://localhost:8083}", path = "/api/cinemas",
        fallbackFactory = CinemaClientFallbackFactory.class)
public interface CinemaClient {

    @GetMapping("/{id}")
    CinemaResponse getCinema(@PathVariable("id") Long id);
}
