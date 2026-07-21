package com.movieticket.bookingservice.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "CINEMA-SERVICE")
public interface CinemaClient {

    @GetMapping("/api/cinemas/{id}")
    CinemaClientResponse getCinema(@PathVariable("id") Long id);
}
