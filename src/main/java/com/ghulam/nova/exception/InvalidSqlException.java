package com.ghulam.nova.exception;

public class InvalidSqlException extends RuntimeException {

    public InvalidSqlException(String message) {
        super(message);
    }
}