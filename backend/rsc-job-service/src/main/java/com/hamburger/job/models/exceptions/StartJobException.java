package com.hamburger.job.models.exceptions;

public class StartJobException extends RuntimeException {
    public StartJobException(String message) {
        super(message);
    }

    public StartJobException(String message, Throwable cause) {
        super(message, cause);
    }
}