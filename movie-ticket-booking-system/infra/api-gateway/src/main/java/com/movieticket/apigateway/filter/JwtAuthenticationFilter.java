package com.movieticket.apigateway.filter;

import com.movieticket.apigateway.utils.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. Lấy JWT từ header Authorization
        String jwt = exchange.getRequest()
                .getHeaders()
                .getFirst(GatewayConstants.HEADER_AUTHOR);

        log.info("===> [Gateway Auth] Nhận được Token từ Client: {}", jwt);

        // 2. Mock xác thực JWT, sau đó gắn thông tin user vào request header
        ServerHttpRequest mutatedRequest = exchange.getRequest()
                .mutate()
                .headers(httpHeaders -> {
                    httpHeaders.add(GatewayConstants.HEADER_USER_ID, "10");
                    httpHeaders.add(GatewayConstants.HEADER_USER_ROLES, "['ROLE_ADMIN']");
                    httpHeaders.add(GatewayConstants.HEADER_USER_NAME, "quanghao@gmail.com");
                })
                .build();

        // 3. Chuyển request đã có header xuống service con
        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return GatewayConstants.ORDER_JWT_AUTH_FILTER;
    }
}