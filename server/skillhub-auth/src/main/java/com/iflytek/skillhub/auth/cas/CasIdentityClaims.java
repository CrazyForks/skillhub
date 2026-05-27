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
        return email != null && !email.isBlank();
    }
}
