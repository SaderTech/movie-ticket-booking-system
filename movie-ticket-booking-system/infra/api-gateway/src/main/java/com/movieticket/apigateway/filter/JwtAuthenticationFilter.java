package com.movieticket.apigateway.filter;

import com.movieticket.apigateway.utils.GatewayConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(GatewayConstants.HEADER_AUTHOR);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("=> [Gateway Auth] Request không có JWT, từ chối truy cập!");
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String userId = claims.get("userId") != null ? claims.get("userId").toString() : "";
            String userEmail = claims.getSubject() != null ? claims.getSubject() : "";

            log.info("===> [Gateway Auth] Xác thực thành công UserID: {} | Email: {}", userId, userEmail);

            // ĐÍNH KÈM THÔNG TIN THẬT VÀO HEADER ĐỂ CHUYỂN XUỐNG DƯỚI
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(GatewayConstants.HEADER_USER_ID, userId)
                    .header(GatewayConstants.HEADER_USER_NAME, userEmail)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            log.error("=> [Gateway Auth] Token giả mạo hoặc đã hết hạn: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return GatewayConstants.ORDER_JWT_AUTH_FILTER;
    }
}