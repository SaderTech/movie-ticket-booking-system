package com.movieticket.bookingservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.timeout:3s}")
    private Duration redisTimeout;

    @Bean
    public RedissonClient redissonClient() {
        int redisTimeoutMillis = Math.toIntExact(redisTimeout.toMillis());

        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisHost + ":" + redisPort)
                .setConnectTimeout(redisTimeoutMillis)
                .setTimeout(redisTimeoutMillis)
                .setRetryAttempts(1)
                .setRetryInterval(1000);
        return Redisson.create(config);
    }
}
