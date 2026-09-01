package com.saveur221.exceptions;

public class MetierException extends RuntimeException {
    public MetierException(String message) {
        super(message);
    }

    public MetierException(String message, Throwable cause) {
        super(message, cause);
    }
}