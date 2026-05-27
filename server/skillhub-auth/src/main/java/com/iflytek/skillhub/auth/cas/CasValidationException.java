package com.iflytek.skillhub.auth.cas;

/**
 * Thrown when CAS ticket validation fails.
 */
public class CasValidationException extends RuntimeException {

    public CasValidationException(String message) {
        super(message);
    }

    public CasValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
