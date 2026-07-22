package com.movieticket.bookingservice.infrastructure.client;

import com.movieticket.bookingservice.infrastructure.client.dto.SeatResponse;
import com.movieticket.bookingservice.infrastructure.client.fallback.SeatClientFallbackFactory;
import com.movieticket.bookingservice.infrastructure.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "cinema-service", configuration = FeignClientConfig.class,
        contextId = "seatClient", path = "/api/seats", fallbackFactory = SeatClientFallbackFactory.class)
public interface SeatClient {

    @GetMapping
    List<SeatResponse> getSeatsByHallId(@RequestParam("hallId") Long hallId);
}
