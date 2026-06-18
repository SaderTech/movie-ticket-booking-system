package com.movieticket.bookingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
    public NewTopic seatHoldCreatedTopic() {
        return TopicBuilder.name("booking.seat-hold.created")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
    public NewTopic seatHoldExpiredTopic() {
        return TopicBuilder.name("booking.seat-hold.expired")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
    public NewTopic bookingConfirmedTopic() {
        return TopicBuilder.name("booking.confirmed")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
    public NewTopic ticketBookedTopic() {
        return TopicBuilder.name("booking.ticket-booked")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = false)
    public NewTopic bookingCancelledTopic() {
        return TopicBuilder.name("booking.cancelled")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
