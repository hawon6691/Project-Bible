package com.projectbible.shop.common.exception;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final Object details;

    public AppException(String code, String message, HttpStatus status) {
        this(code, message, status, null);
    }

    public AppException(String code, String message, HttpStatus status, Object details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public Object getDetails() {
        return details;
    }
}
