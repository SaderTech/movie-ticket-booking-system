package com.movieticket.apigateway.filter;

import com.movieticket.apigateway.utils.GatewayConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filter #2 — Security Headers
 *
 * Mục đích: Tự động thêm các HTTP Security Header vào MỌI response.
 * Giúp bảo vệ client khỏi các tấn công phổ biến:
 *  - Clickjacking (X-Frame-Options)
 *  - MIME sniffing (X-Content-Type-Options)
 *  - XSS (X-XSS-Protection)
 *  - Downgrade attack (Strict-Transport-Security)
 */

@Slf4j
@Component
public class SecurityHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Đăng ký add security headers NGAY TRƯỚC KHI response bị commit
        exchange.getResponse().beforeCommit(() -> {
            addSecurityHeaders(exchange.getResponse());
            return Mono.empty();
        });

        return chain.filter(exchange);
    }

    private void addSecurityHeaders(ServerHttpResponse response) {
        MultiValueMap<String, String> headers = response.getHeaders();

        // Ngăn trang bị nhúng trong <iframe> -> chống Clickjacking
        headers.add(GatewayConstants.HEADER_X_FRAME_OPTIONS, GatewayConstants.VALUE_DENY);

        // Ngăn browser đoán content-type -> chống MIME sniffing
        headers.add(GatewayConstants.HEADER_X_CONTENT_TYPE_OPTIONS, GatewayConstants.VALUE_NOSNIFF);

        // Bật XSS filter của browser
        headers.add(GatewayConstants.HEADER_X_XSS_PROTECTION, GatewayConstants.VALUE_XSS_BLOCK);

        headers.add(GatewayConstants.HEADER_STRICT_TRANSPORT, GatewayConstants.VALUE_HSTS);

        // Kiểm soát thông tin Referer khi navigate
        headers.add(GatewayConstants.HEADER_REFERRER_POLICY, GatewayConstants.VALUE_REFERRER);

        // Không cache response nhạy cảm ở browser
        headers.add(GatewayConstants.HEADER_CACHE_CONTROL, GatewayConstants.VALUE_NO_CACHE);

        log.debug("Security headers added to response.");
    }

    @Override
    public int getOrder() {
        return GatewayConstants.ORDER_SECURITY_HEADER; // Order = -90 (chạy trước Auth -50)
    }
}