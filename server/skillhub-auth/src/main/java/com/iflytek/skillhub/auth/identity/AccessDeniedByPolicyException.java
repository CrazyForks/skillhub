package com.iflytek.skillhub.auth.identity;

/**
 * Thrown when an authenticated upstream identity is rejected by the configured AccessPolicy.
 */
public class AccessDeniedByPolicyException extends RuntimeException {
    public AccessDeniedByPolicyException() {
        super("Access denied by policy");
    }
}
