package com.movieticket.apigateway.utils;

public class GatewayConstants {

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";

    // Danh sách các HTTP Headers
    public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

    public static final String HEADER_USER_ID = "X-User-ID";
    public static final String HEADER_USER_NAME = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_AUTHOR = "Authorization";

    //Danh sách các Security Headers
    public static final String HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
    public static final String HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
    public static final String HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
    public static final String HEADER_STRICT_TRANSPORT = "Strict-Transport-Security";    public static final String HEADER_REFERRER_POLICY = "Referrer-Policy";
    public static final String HEADER_CACHE_CONTROL = "Cache-Control";

    public static final String VALUE_DENY = "DENY";
    public static final String VALUE_NOSNIFF = "nosniff";
    public static final String VALUE_XSS_BLOCK = "1; mode=block";
    public static final String VALUE_HSTS = "max-age=31536000; includeSubDomains";
    public static final String VALUE_REFERRER = "strict-origin-when-cross-origin";
    public static final String VALUE_NO_CACHE = "no-store, no-cache, must-revalidate";

    // Thứ tự ưu tiên
    public static final int ORDER_CORRELATION_FILTER = -200;
    public static final int ORDER_LOGGING_FILTER = -100;
    public static final int ORDER_SECURITY_HEADER = -90;
    public static final int ORDER_AUTHENTICATION = -80;
    public static final int ORDER_AUTHORIZATION = -70;
    public static final int ORDER_JWT_AUTH_FILTER = -50;

}
