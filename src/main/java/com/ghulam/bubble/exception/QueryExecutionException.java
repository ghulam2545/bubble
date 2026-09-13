package com.ghulam.bubble.exception;

import lombok.Getter;

@Getter
public class QueryExecutionException extends RuntimeException {
    private final String sql;

    public QueryExecutionException(String message, String sql) {
        super(message);
        this.sql = sql;
    }

    public QueryExecutionException(String message, String sql, Throwable cause) {
        super(message, cause);
        this.sql = sql;
    }
}