package com.movieticket.notificationservice.utils;

public final class Constants {

    public static final String SERVICE_NAME = "notification-service";

    public static final String TOPIC_BOOKING_CREATED = "booking-created";
    public static final String TOPIC_PAYMENT_SUCCESS = "payment-success";

    public static final String DEFAULT_CHANNEL = "EMAIL";
    public static final String DEFAULT_NOTIFICATION_TYPE = "SYSTEM_ALERT";

    private Constants() {
    }
}