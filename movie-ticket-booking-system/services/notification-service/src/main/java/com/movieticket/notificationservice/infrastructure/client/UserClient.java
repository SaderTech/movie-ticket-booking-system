package com.movieticket.notificationservice.infrastructure.client;

import com.movieticket.notificationservice.infrastructure.client.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", path = "/api/users")
public interface UserClient {

    @GetMapping("/{id}")
    UserResponse getUserById(@PathVariable("id") Long id);
}
