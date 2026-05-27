package com.iflytek.skillhub.auth.identity;

import com.iflytek.skillhub.auth.oauth.AccountPendingException;
import com.iflytek.skillhub.auth.policy.AccessDecision;
import com.iflytek.skillhub.auth.policy.AccessPolicy;
import com.iflytek.skillhub.auth.rbac.PlatformPrincipal;
import com.iflytek.skillhub.domain.user.UserStatus;
import org.springframework.stereotype.Service;

/**
 * Provider-neutral identity authentication: evaluates access policy and creates or binds a
 * platform principal. Used by all upstream identity flows (OAuth, CAS, etc.) so that the same
 * allow/deny/pending decisions apply regardless of protocol.
 */
@Service
public class IdentityAuthenticator {

    private final AccessPolicy accessPolicy;
    private final IdentityBindingService identityBindingService;

    public IdentityAuthenticator(AccessPolicy accessPolicy, IdentityBindingService identityBindingService) {
        this.accessPolicy = accessPolicy;
        this.identityBindingService = identityBindingService;
    }

    /**
     * Evaluates policy and returns a platform principal for an allowed identity.
     *
     * @throws AccountPendingException if the policy yields PENDING_APPROVAL
     * @throws AccessDeniedByPolicyException if the policy yields DENY
     * @throws com.iflytek.skillhub.auth.oauth.AccountDisabledException if the user is disabled
     */
    public PlatformPrincipal authenticate(IdentityClaims claims) {
        AccessDecision decision = accessPolicy.evaluate(claims);

        if (decision == AccessDecision.PENDING_APPROVAL) {
            identityBindingService.createPendingUserIfAbsent(claims);
            throw new AccountPendingException();
        }
        if (decision == AccessDecision.DENY) {
            throw new AccessDeniedByPolicyException();
        }

        return identityBindingService.bindOrCreate(claims, UserStatus.ACTIVE);
    }
}
