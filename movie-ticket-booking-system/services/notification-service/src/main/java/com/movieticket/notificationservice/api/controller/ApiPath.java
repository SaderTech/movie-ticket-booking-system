package com.movieticket.notificationservice.api.controller;

public final class ApiPath {

    public static final String BASE = "/api/v1";

    public static final String NOTIFICATIONS = BASE + "/notifications";
    public static final String TEMPLATES = BASE + "/notification-templates";
    public static final String QR_CODES = BASE + "/qr-codes";

    public static final String SEND = "/send";
    public static final String LOGS = "/logs";
    public static final String TICKET = "/ticket/{ticketCode}";

    private ApiPath() {
    }
}