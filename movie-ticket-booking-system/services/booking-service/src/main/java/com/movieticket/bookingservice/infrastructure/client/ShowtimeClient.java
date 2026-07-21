package com.movieticket.bookingservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "SHOWTIME-SERVICE", path = "/api/showtimes")
public interface ShowtimeClient {

    @GetMapping("/{id}")
    Map<String, Object> getShowtime(@PathVariable("id") Long id);
}
