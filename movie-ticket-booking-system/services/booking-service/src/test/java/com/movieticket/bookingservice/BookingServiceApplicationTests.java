package com.movieticket.bookingservice;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;

@SpringBootTest
class BookingServiceApplicationTests {

    @MockBean
    private RedissonClient redissonClient;

    @MockBean
    @org.springframework.beans.factory.annotation.Qualifier("jsonKafkaTemplate")
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void contextLoads() {
    }

}
