package com.movieticket.bookingservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "cinema-service", path = "/api/v1/cinemas")
public interface CinemaClient {

    @GetMapping("/{id}")
    Map<String, Object> getCinema(@PathVariable("id") Long id);
}
