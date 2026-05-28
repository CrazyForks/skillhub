package com.iflytek.skillhub.auth.identity;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * Thrown when an authenticated external identity maps to a platform account that is pending approval.
 * Used by both OAuth and CAS flows.
 */
public class AccountPendingException extends OAuth2AuthenticationException {

    public AccountPendingException() {
        super(new OAuth2Error("account_pending", "Account pending approval", null));
    }
}
