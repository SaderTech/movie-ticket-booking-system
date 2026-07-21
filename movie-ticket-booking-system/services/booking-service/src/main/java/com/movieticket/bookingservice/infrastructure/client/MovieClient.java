package com.movieticket.bookingservice.infrastructure.client;

import com.movieticket.bookingservice.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "MOVIE-SERVICE", configuration = FeignClientConfig.class)
public interface MovieClient {

    @GetMapping("/api/movies/demo-receive")
    String callMovieDemo();

    @GetMapping("/api/movies/{id}")
    Map<String, Object> getMovie(@PathVariable("id") Long id);
}