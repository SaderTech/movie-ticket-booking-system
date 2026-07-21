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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Arrays;
import java.util.Collection;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${app.jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        HttpMethod method = exchange.getRequest().getMethod();

        if (HttpMethod.OPTIONS.equals(method) || isPublicCinemaRead(path, method)) {
            return chain.filter(exchange);
        }

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

            if (isCinemaApiPath(path) && isWriteMethod(method) && !hasAdminRole(claims)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            String userRoles = rolesAsHeader(claims);

            log.info("===> [Gateway Auth] Xác thực thành công UserID: {} | Email: {}", userId, userEmail);

            // ĐÍNH KÈM THÔNG TIN THẬT VÀO HEADER ĐỂ CHUYỂN XUỐNG DƯỚI
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header(GatewayConstants.HEADER_USER_ID, userId)
                    .header(GatewayConstants.HEADER_USER_NAME, userEmail)
                    .header(GatewayConstants.HEADER_USER_ROLES, userRoles)
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

    private boolean isPublicCinemaRead(String path, HttpMethod method) {
        if (!HttpMethod.GET.equals(method)) {
            return false;
        }
        return path.startsWith("/api/cinemas")
                || path.startsWith("/api/halls")
                || path.startsWith("/api/seats")
                || path.startsWith("/api/seat-types");
    }

    private boolean isCinemaApiPath(String path) {
        return path.startsWith("/api/cinemas")
                || path.startsWith("/api/halls")
                || path.startsWith("/api/seats")
                || path.startsWith("/api/seat-types")
                || path.startsWith("/api/hall-maintenances");
    }

    private boolean isWriteMethod(HttpMethod method) {
        return HttpMethod.POST.equals(method)
                || HttpMethod.PUT.equals(method)
                || HttpMethod.PATCH.equals(method)
                || HttpMethod.DELETE.equals(method);
    }

    private boolean hasAdminRole(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof Collection<?> roles) {
            return roles.stream().map(String::valueOf).anyMatch(this::isAdminRole);
        }

        Object roleClaim = rolesClaim != null ? rolesClaim : claims.get("role");
        if (roleClaim == null) {
            return false;
        }

        return Arrays.stream(String.valueOf(roleClaim).split("[,\\s]+"))
                .anyMatch(this::isAdminRole);
    }

    private boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(role) || "ROLE_ADMIN".equalsIgnoreCase(role);
    }

    private String rolesAsHeader(Claims claims) {
        Object roles = claims.get("roles");
        if (roles == null) {
            roles = claims.get("role");
        }
        return roles == null ? "" : String.valueOf(roles);
    }
}
