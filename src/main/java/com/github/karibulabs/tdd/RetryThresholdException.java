package com.github.karibulabs.tdd;

public class RetryThresholdException extends RuntimeException {

    public RetryThresholdException(String message) {
        super(message);
    }
}
