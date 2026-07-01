package com.movieticket.apigateway.utils;

public class GatewayConstants {

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    // Danh sách các HTTP Headers tiêu chuẩn dùng để truyền thông tin qua lại giữa các Microservices
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";
    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_USER_NAME = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_AUTHOR = "Authorization";

    // Thứ tự ưu tiên
    public static final int ORDER_CORRELATION_FILTER = -200;
    public static final int ORDER_LOGGING_FILTER = -100;
    public static final int ORDER_JWT_AUTH_FILTER = -90;

}
