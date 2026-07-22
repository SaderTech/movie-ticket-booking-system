package com.movieticket.bookingservice.api.exception;

public enum ErrorCode {
    SEAT_ALREADY_HELD("SEAT_ALREADY_HELD", "Seat(s) already held by another user"),
    HOLD_EXPIRED("HOLD_EXPIRED", "Seat hold has expired"),
    INVALID_HOLD_TOKEN("INVALID_HOLD_TOKEN", "Invalid hold token"),
    BOOKING_NOT_FOUND("BOOKING_NOT_FOUND", "Booking not found"),
    BOOKING_ALREADY_CONFIRMED("BOOKING_ALREADY_CONFIRMED", "Booking is already confirmed"),
    BOOKING_CANNOT_BE_CANCELLED("BOOKING_CANNOT_BE_CANCELLED", "Booking cannot be cancelled in current state"),
    BOOKING_CONFLICT("BOOKING_CONFLICT", "Booking conflict, please retry"),
    SEAT_UNAVAILABLE("SEAT_UNAVAILABLE", "Requested seat(s) are not available"),
    HOLD_NOT_FOUND("HOLD_NOT_FOUND", "Seat hold not found"),
    SEAT_ALREADY_BOOKED("SEAT_ALREADY_BOOKED", "Seat is already booked"),
    PAYMENT_FAILED("PAYMENT_FAILED", "Payment processing failed"),
    SHOWTIME_NOT_FOUND("SHOWTIME_NOT_FOUND", "Showtime not found"),
    CINEMA_NOT_FOUND("CINEMA_NOT_FOUND", "Cinema not found"),
    INVALID_REQUEST("INVALID_REQUEST", "Invalid request"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized access"),
    FORBIDDEN("FORBIDDEN", "Forbidden access");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}
