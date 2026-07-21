package com.movieticket.bookingservice.infrastructure.client;

public record CinemaClientResponse(
        Long id,
        String name,
        String address,
        String city,
        String status
) {
}
