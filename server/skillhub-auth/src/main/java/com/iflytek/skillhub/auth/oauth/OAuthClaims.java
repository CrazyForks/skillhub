package com.iflytek.skillhub.auth.oauth;

import com.iflytek.skillhub.auth.identity.IdentityClaims;
import java.util.Map;

/**
 * Normalized identity claims extracted from an OAuth provider before local account decisions are
 * made.
 */
public record OAuthClaims(
    String provider,
    String subject,
    String email,
    boolean emailVerified,
    String providerLogin,
    Map<String, Object> extra
) implements IdentityClaims {}
