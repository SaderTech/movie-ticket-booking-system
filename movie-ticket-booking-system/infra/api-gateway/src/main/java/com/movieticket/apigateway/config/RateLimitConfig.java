package com.movieticket.apigateway.config;

import com.movieticket.apigateway.utils.GatewayConstants;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimitConfig {

    /**
     * Limits anonymous traffic by source IP. It is deliberately used for login
     * and registration, where an authenticated user ID is not available yet.
     */
    @Bean
    public KeyResolver clientIpKeyResolver() {
        return exchange -> Mono.just("ip:" + clientIp(exchange));
    }

    /**
     * Applies an independent quota to each authenticated user. The JWT filter
     * removes any client-supplied X-User-ID and inserts the verified value before
     * route filters run. Public requests fall back to their source IP.
     */
    @Bean
    @Primary
    public KeyResolver userOrClientKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders()
                    .getFirst(GatewayConstants.HEADER_USER_ID);

            String key = userId == null || userId.isBlank()
                    ? "ip:" + clientIp(exchange)
                    : "user:" + userId;
            return Mono.just(key);
        };
    }

    private String clientIp(org.springframework.web.server.ServerWebExchange exchange) {
        var remoteAddress = exchange.getRequest().getRemoteAddress();
        return remoteAddress != null && remoteAddress.getAddress() != null
                ? remoteAddress.getAddress().getHostAddress()
                : "unknown-client";
    }
}
