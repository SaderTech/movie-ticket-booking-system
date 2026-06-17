package com.movieticket.apigateway.filter;

import com.movieticket.apigateway.utils.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.movieticket.apigateway.utils.GatewayConstants.HEADER_CORRELATION_ID;

@Component
@Slf4j
public class CorrelationIdFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String correlationId = request.getHeaders().getFirst(HEADER_CORRELATION_ID);

        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
            log.debug("Generated new Correlation ID: {}", correlationId);
        }

        final String finalCorrelationId = correlationId;

        ServerHttpRequest newRequest = exchange.getRequest().mutate()
                .header(HEADER_CORRELATION_ID, finalCorrelationId)
                .build();

        return chain.filter(exchange.mutate().request(newRequest).build())
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    if (!response.isCommitted()) {
                        response.getHeaders().set(HEADER_CORRELATION_ID, finalCorrelationId);
                    }
                }));
    }

    @Override
    public int getOrder() {
        return GatewayConstants.ORDER_CORRELATION_FILTER;
    }

}
