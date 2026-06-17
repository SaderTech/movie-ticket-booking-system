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
        // 1. Trích xuất JWT Token từ Header "Authorization" do Client gửi lên
        String jwt = exchange.getRequest().getHeaders().getFirst(GatewayConstants.HEADER_AUTHOR);

        log.info("===> [Gateway Auth] Nhận được Token từ Client: {}", jwt);

        // 2. Giả lập xác thực (Mock): Tự động đính kèm thông tin User vào Request Headers
        ServerHttpRequest muteRequest = exchange.getRequest().mutate().headers((httpHeaders -> {
            httpHeaders.add(GatewayConstants.HEADER_USER_ID, "10");
            httpHeaders.add(GatewayConstants.HEADER_USER_ROLES, "['ROLE_ADMIN']");
            httpHeaders.add(GatewayConstants.HEADER_USER_NAME, "quanghao@gmail.com"); // Đổi sang email của bạn để demo cho chất
        })).build();

        // 3. Tiếp tục chuyển tiếp Request đã có sẵn thông tin User xuống các Service con
        return chain.filter(exchange.mutate().request(muteRequest).build());
    }

    @Override
    public int getOrder() {
        return GatewayConstants.ORDER_JWT_AUTH_FILTER;
    }
}
