package com.iflytek.skillhub.auth.identity;

import java.util.Map;

/**
 * Provider-neutral identity claims extracted from external authentication systems.
 * Implementations adapt provider-specific formats (OAuth2, CAS, SAML, etc.) to this common interface.
 */
public interface IdentityClaims {

    /**
     * Provider identifier (e.g., "github", "gitlab", "cas").
     */
    String provider();

    /**
     * Unique subject identifier from the provider.
     * Must be stable across logins for the same user.
     */
    String subject();

    /**
     * User's email address (may be null if provider doesn't expose it).
     */
    String email();

    /**
     * Whether the email has been verified by the provider.
     */
    boolean emailVerified();

    /**
     * Display name or username from the provider.
     */
    String providerLogin();

    /**
     * Additional provider-specific attributes (e.g., avatar_url, groups, custom claims).
     */
    Map<String, Object> extra();
}
