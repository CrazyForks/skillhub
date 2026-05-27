package com.iflytek.skillhub.auth.policy;

import com.iflytek.skillhub.auth.identity.IdentityClaims;
import java.util.Set;

/**
 * Access policy that limits login to explicitly allowed identity providers.
 */
public class ProviderAllowlistAccessPolicy implements AccessPolicy {
    private final Set<String> allowedProviders;

    public ProviderAllowlistAccessPolicy(Set<String> allowedProviders) {
        this.allowedProviders = allowedProviders;
    }

    @Override
    public AccessDecision evaluate(IdentityClaims claims) {
        return allowedProviders.contains(claims.provider())
            ? AccessDecision.ALLOW : AccessDecision.DENY;
    }
}
