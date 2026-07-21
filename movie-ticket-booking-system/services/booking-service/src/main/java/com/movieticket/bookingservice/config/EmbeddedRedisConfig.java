package com.movieticket.bookingservice.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.net.ServerSocket;

@Configuration
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class EmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void startRedis() {
        if (isPortAvailable(6379)) {
            try {
                redisServer = RedisServer.newRedisServer()
                        .port(6379)
                        .setting("maxmemory 128M")
                        .build();
                redisServer.start();
                log.info("Embedded Redis started on port 6379");
            } catch (Exception e) {
                log.warn("Could not start embedded Redis: {}. If you have Redis running externally, ignore this.", e.getMessage());
            }
        } else {
            log.info("Port 6379 already in use – using existing Redis instance");
        }
    }

    @PreDestroy
    public void stopRedis() {
        if (redisServer != null && redisServer.isActive()) {
            try {
                redisServer.stop();
                log.info("Embedded Redis stopped");
            } catch (IOException e) {
                log.warn("Error stopping embedded Redis: {}", e.getMessage());
            }
        }
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
