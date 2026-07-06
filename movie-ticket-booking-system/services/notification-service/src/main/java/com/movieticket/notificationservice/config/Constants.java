package com.movieticket.notificationservice.config;

public final class Constants {

    public static final String SERVICE_NAME = "notification-service";

    public static final String TOPIC_BOOKING_CONFIRMED = "booking.confirmed";
    public static final String TOPIC_BOOKING_CANCELLED = "booking.cancelled";
    public static final String TOPIC_SEAT_HOLD_CREATED = "booking.seat-hold.created";
    public static final String TOPIC_SEAT_HOLD_EXPIRED = "booking.seat-hold.expired";
    public static final String TOPIC_PAYMENT_SUCCESS = "payment.success";
    public static final String TOPIC_PAYMENT_FAILED = "payment.failed";
    public static final String TOPIC_SHOWTIME_REMINDER = "notification.showtime-reminder";

    public static final String DEFAULT_CHANNEL = "EMAIL";
    public static final String DEFAULT_NOTIFICATION_TYPE = "SYSTEM_ALERT";

    private Constants() {
    }
}
