package com.movieticket.bookingservice.infrastructure.client;

import com.movieticket.bookingservice.infrastructure.client.dto.ShowtimeResponse;
import com.movieticket.bookingservice.infrastructure.client.fallback.ShowtimeClientFallbackFactory;
import com.movieticket.bookingservice.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "showtime-service", configuration = FeignClientConfig.class,
        path = "/api/showtimes", fallbackFactory = ShowtimeClientFallbackFactory.class)
public interface ShowtimeClient {

    @GetMapping("/{id}")
    ShowtimeResponse getShowtime(@PathVariable("id") Long id);
}
