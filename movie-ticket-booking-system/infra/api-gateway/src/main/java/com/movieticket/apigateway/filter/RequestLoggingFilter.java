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
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Lấy thông tin Request đầu vào (Pre-filter)
        ServerHttpRequest serverHttpRequest = exchange.getRequest();

        String correlationId = serverHttpRequest.getHeaders().getFirst(GatewayConstants.HEADER_CORRELATION_ID);
        log.info("Request header [{}]: Path called -> {}", correlationId, serverHttpRequest.getPath());

        // Tiếp tục chuyển tiếp request sang các filter tiếp theo (Post-filter)
        return chain.filter(exchange.mutate().build());
    }

    @Override
    public int getOrder() {
        return GatewayConstants.ORDER_LOGGING_FILTER;
    }

}
