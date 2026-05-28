package com.iflytek.skillhub.auth.cas;

import com.iflytek.skillhub.auth.identity.IdentityClaims;

import java.util.Map;

/**
 * Adapts CAS ticket validation attributes to the platform-neutral IdentityClaims interface.
 */
public record CasIdentityClaims(
    String subject,
    String email,
    String providerLogin,
    Map<String, Object> extra
) implements IdentityClaims {

    public static final String PROVIDER = "cas";

    @Override
    public String provider() {
        return PROVIDER;
    }

    @Override
    public boolean emailVerified() {
        // CAS protocol does not verify email addresses — the attribute is passed through from the
        // upstream directory (LDAP, AD, etc.) without cryptographic proof. Return false to prevent
        // AccessPolicy implementations from trusting unverified claims.
        return false;
    }
}
