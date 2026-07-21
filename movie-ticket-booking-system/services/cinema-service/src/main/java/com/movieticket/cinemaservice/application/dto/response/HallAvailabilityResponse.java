package com.movieticket.cinemaservice.application.dto.response;

public record HallAvailabilityResponse(
        boolean available,
        String reason
) {

    public static HallAvailabilityResponse availableResult() {
        return new HallAvailabilityResponse(true, null);
    }

    public static HallAvailabilityResponse unavailable(String reason) {
        return new HallAvailabilityResponse(false, reason);
    }
}
