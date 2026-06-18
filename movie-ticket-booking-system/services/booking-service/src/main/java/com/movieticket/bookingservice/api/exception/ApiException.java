package com.movieticket.bookingservice.api.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;
    private final int status;

    public ApiException(ErrorCode errorCode, int status, String message) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public ApiException(ErrorCode errorCode, String message) {
        this(errorCode, 400, message);
    }

    public ApiException(ErrorCode errorCode) {
        this(errorCode, 400, errorCode.getDefaultMessage());
    }
}
