package com.movieticket.bookingservice.api.controller;

import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final MovieClient movieClient;

    @GetMapping("/demo")
    public String testDemo(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        String userId = request.getHeader("X-User-ID");
        String userEmail = request.getHeader("X-User-Email");

        log.info("============== BOOKING SERVICE ==============");
        log.info("=> [1. Booking Service] Nhận request từ Gateway");
        log.info("=> Correlation ID: {}", correlationId);
        log.info("=> User ID: {}", userId);
        log.info("=> User Email: {}", userEmail);
        log.info("=> Đang gọi Movie Service bằng Feign Client...");
        log.info("=============================================");

        String result = movieClient.callMovieDemo();

        return "Hệ thống kết nối mượt mà! " + result;
    }
}