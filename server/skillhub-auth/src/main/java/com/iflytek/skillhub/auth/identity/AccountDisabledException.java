package com.iflytek.skillhub.auth.identity;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

/**
 * Thrown when an authenticated external identity maps to a platform account that is disabled.
 * Used by both OAuth and CAS flows.
 */
public class AccountDisabledException extends OAuth2AuthenticationException {

    public AccountDisabledException() {
        super(new OAuth2Error("account_disabled", "Account is disabled", null));
    }
}
