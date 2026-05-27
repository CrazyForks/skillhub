package com.iflytek.skillhub.auth.policy;

import com.iflytek.skillhub.auth.identity.IdentityClaims;
import java.util.Set;

/**
 * Access policy that only permits a configured set of provider-subject pairs.
 */
public class SubjectWhitelistAccessPolicy implements AccessPolicy {
    private final Set<String> whitelistedSubjects;

    public SubjectWhitelistAccessPolicy(Set<String> whitelistedSubjects) {
        this.whitelistedSubjects = whitelistedSubjects;
    }

    @Override
    public AccessDecision evaluate(IdentityClaims claims) {
        String key = claims.provider() + ":" + claims.subject();
        return whitelistedSubjects.contains(key)
            ? AccessDecision.ALLOW : AccessDecision.DENY;
    }
}
