package com.iflytek.skillhub.auth.policy;

import com.iflytek.skillhub.auth.identity.IdentityClaims;

/**
 * Access policy that accepts all externally authenticated users.
 */
public class OpenAccessPolicy implements AccessPolicy {
    @Override
    public AccessDecision evaluate(IdentityClaims claims) {
        return AccessDecision.ALLOW;
    }
}
