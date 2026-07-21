package com.movieticket.apigateway.filter;

import com.movieticket.apigateway.utils.GatewayConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class GlobalFilterLogging implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(GlobalFilterLogging.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = (ServerHttpRequest) exchange.getRequest();
        long startTime = System.currentTimeMillis();
        log.info("""
                Incoming request:
                Method={}
                URI={}
                ClientIP={}
                """,
                request.getMethod(),
                request.getURI(),
                request.getRemoteAddress());
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    long duration = System.currentTimeMillis() - startTime;
                    log.info("""
                            Outgoing Response:
                            Status={}
                            Duration={} ms
                            """,
                            exchange.getResponse().getStatusCode(), duration);
                }));
    }

    @Override
    public int getOrder() {
        return GatewayConstants.ORDER_LOGGING_FILTER;
    }
}
