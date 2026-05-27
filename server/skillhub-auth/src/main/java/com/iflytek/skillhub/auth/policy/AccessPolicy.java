package com.iflytek.skillhub.auth.policy;

import com.iflytek.skillhub.auth.identity.IdentityClaims;

/**
 * Policy contract for deciding whether externally authenticated users may enter the platform.
 */
public interface AccessPolicy {
    AccessDecision evaluate(IdentityClaims claims);
}
