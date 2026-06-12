package com.threasury.compliance.exception;

public class ProcessingTimeoutException extends RuntimeException {

    public ProcessingTimeoutException(String message) {
        super(message);
    }

    public ProcessingTimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
