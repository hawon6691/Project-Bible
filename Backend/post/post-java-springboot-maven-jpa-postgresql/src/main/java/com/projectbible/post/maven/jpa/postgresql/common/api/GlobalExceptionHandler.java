package com.projectbible.post.maven.jpa.postgresql.common.api;

import com.projectbible.post.maven.jpa.postgresql.common.exception.AppException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> app(AppException e) {
        return ResponseEntity.status(e.getStatus()).body(ApiResponse.failure(e.getCode(), e.getMessage(), e.getDetails()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> bad(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(ApiResponse.failure("VALIDATION_ERROR", e.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> err(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.failure("INTERNAL_SERVER_ERROR", "Unexpected error", null));
    }
}
