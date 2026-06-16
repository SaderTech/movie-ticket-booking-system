package com.movieticket.cinemaservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI cinemaServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cinema Service API")
                        .version("1.0.0")
                        .description("API for managing cinemas, halls, seats, seat types and hall maintenance"));
    }
}