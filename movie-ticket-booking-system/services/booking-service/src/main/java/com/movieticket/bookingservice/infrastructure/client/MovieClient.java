package com.movieticket.bookingservice.infrastructure.client;

import com.movieticket.bookingservice.infrastructure.client.dto.MovieResponse;
import com.movieticket.bookingservice.infrastructure.client.fallback.MovieClientFallbackFactory;
import com.movieticket.bookingservice.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "MOVIE-SERVICE", configuration = FeignClientConfig.class,
        fallbackFactory = MovieClientFallbackFactory.class)
public interface MovieClient {

    @GetMapping("/api/movies/demo-receive")
    String callMovieDemo();

    @GetMapping("/api/movies/{id}")
    MovieResponse getMovie(@PathVariable("id") Long id);
}
