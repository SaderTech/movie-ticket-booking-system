package com.movieticket.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(exclude = {
        RedisAutoConfiguration.class,
        RedisReactiveAutoConfiguration.class
})
@EnableDiscoveryClient
public class ApiGatewayApplication {

    public static void main(String[] args) {
        System.out.println("DEBUG JWT_SECRET: " + System.getenv("JWT_SECRET"));
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

}
