package com.movieticket.bookingservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
public class RedissonConfig {

    @Bean
    @DependsOn("embeddedRedisConfig")
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
