package com.movieticket.bookingservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    @ConditionalOnProperty(name = "app.redis.enabled", havingValue = "true", matchIfMissing = false)
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://localhost:6379")
                .setConnectTimeout(3000)
                .setTimeout(3000)
                .setRetryAttempts(1)
                .setRetryInterval(1000);
        return Redisson.create(config);
    }
}
