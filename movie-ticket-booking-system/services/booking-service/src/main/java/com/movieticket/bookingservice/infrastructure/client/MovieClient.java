package com.movieticket.bookingservice.infrastructure.client;

import com.movieticket.bookingservice.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "MOVIE-SERVICE")
public interface MovieClient {

    @GetMapping("/api/movies/demo-receive")
    String callMovieDemo();
}
