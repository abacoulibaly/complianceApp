package com.threasury.compliance.exception;

public class ComplianceProcessingException extends RuntimeException {

    public ComplianceProcessingException(String message) {
        super(message);
    }

    public ComplianceProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
