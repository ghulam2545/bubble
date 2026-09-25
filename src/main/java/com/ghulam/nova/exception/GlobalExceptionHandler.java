package com.ghulam.nova.exception;

import com.ghulam.nova.dtos.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<ApiError> handleDatabaseException(DatabaseException ex, HttpServletRequest request) {
        log.error("[DatabaseException] {} {} → {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database Error", ex.getMessage(), request);
    }

    @ExceptionHandler(QueryExecutionException.class)
    public ResponseEntity<ApiError> handleQueryExecutionException(QueryExecutionException ex, HttpServletRequest request) {
        log.error("[QueryExecutionException] {} {} | sql='{}' → {}", request.getMethod(), request.getRequestURI(), ex.getSql(), ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Query Execution Error", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidSqlException.class)
    public ResponseEntity<ApiError> handleInvalidSqlException(InvalidSqlException ex, HttpServletRequest request) {
        log.error("[InvalidSqlException] {} {} → {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid SQL", ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("[ResourceNotFoundException] {} {} → {}", request.getMethod(), request.getRequestURI(), ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("[UnhandledException] {} {} → {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", request);
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String error, String message, HttpServletRequest request) {
        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(request.getRequestURI())
                .requestId(UUID.randomUUID().toString())
                .build();
        return new ResponseEntity<>(apiError, status);
    }
}