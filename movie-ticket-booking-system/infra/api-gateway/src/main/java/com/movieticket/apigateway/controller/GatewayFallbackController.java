package com.movieticket.apigateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Produces an explicit, non-cacheable response when a public read route is
 * unavailable. Fallbacks must never return fabricated business data.
 */
@RestController
@Slf4j
@RequestMapping(path = "/fallback", produces = MediaType.APPLICATION_JSON_VALUE)
public class GatewayFallbackController {

    @RequestMapping("/movie")
    public ResponseEntity<Map<String, Object>> movieFallback(ServerWebExchange exchange) {
        return unavailable("MOVIE-SERVICE", exchange);
    }

    @RequestMapping("/cinema")
    public ResponseEntity<Map<String, Object>> cinemaFallback(ServerWebExchange exchange) {
        return unavailable("CINEMA-SERVICE", exchange);
    }

    @RequestMapping("/showtime")
    public ResponseEntity<Map<String, Object>> showtimeFallback(ServerWebExchange exchange) {
        return unavailable("SHOWTIME-SERVICE", exchange);
    }

    private ResponseEntity<Map<String, Object>> unavailable(String service, ServerWebExchange exchange) {
        Throwable cause = exchange.getAttribute(
                ServerWebExchangeUtils.CIRCUITBREAKER_EXECUTION_EXCEPTION_ATTR);
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-ID");
        String path = exchange.getRequest().getURI().getPath();

        log.warn("Circuit breaker fallback for {} | path={} | correlationId={} | cause={}",
                service, path, correlationId, cause == null ? "circuit open" : cause.getClass().getSimpleName());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase());
        body.put("service", service);
        body.put("message", service + " is temporarily unavailable. Please try again later.");
        body.put("path", path);
        body.put("correlationId", correlationId);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .cacheControl(org.springframework.http.CacheControl.noStore())
                .body(body);
    }
}
