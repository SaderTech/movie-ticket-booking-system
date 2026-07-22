package com.movieticket.userservice.config;





import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenApiConfig {


    @Bean
    public OpenAPI userApi() {

        return new OpenAPI()
                .info(new Info()
                        .title("Movie Ticket Booking System - User Service API")
                        .version("1.0.0")
                        .description("""
                                REST API documentation for User Service.

                                Features:
                                - User Registration
                                - User Authentication
                                - User Management
                                - User Profile
                                """)
                        .contact(new Contact()
                                .name("Movie Ticket Team")
                                .email("team@movie.com"))
                )
                .externalDocs(
                        new ExternalDocumentation()
                                .description("Movie Ticket Booking Documentation")
                );
    }

}