package com.movieticket.bookingservice.api.controller;

import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
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
        log.info("=> [1. Booking Service] Đã nhận dữ liệu từ Gateway. Mã ID: {}. Đang gọi Feign Client...", correlationId);
        String result = movieClient.callMovieDemo();
        return "Hệ thống kết nối mượt mà! " + result;
    }
}
