package com.movieticket.movieservice.interfaces.rest;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movies")
@Slf4j
public class MoviePropagationDemoController {

    @GetMapping("/demo-receive")
    public String receiveDemo(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        String userId = request.getHeader("X-User-ID");
        String userEmail = request.getHeader("X-User-Email");

        log.info("============== KẾT QUẢ PROPAGATION ==============");
        log.info("=> [2. Movie Service] Nhận lệnh Feign thành công!");
        log.info("=> Mã định danh hệ thống (Correlation ID): {}", correlationId);
        log.info("=> Mã người dùng (User ID): {}", userId);
        log.info("=> Email người dùng: {}", userEmail);
        log.info("=================================================");

        return "[Movie Service phản hồi] Đã nhận được mã: " + correlationId;
    }
}