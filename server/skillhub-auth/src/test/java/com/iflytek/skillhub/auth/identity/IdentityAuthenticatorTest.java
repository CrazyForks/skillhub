package com.iflytek.skillhub.auth.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iflytek.skillhub.auth.identity.AccountPendingException;
import com.iflytek.skillhub.auth.oauth.OAuthClaims;
import com.iflytek.skillhub.auth.policy.AccessDecision;
import com.iflytek.skillhub.auth.policy.AccessPolicy;
import com.iflytek.skillhub.auth.rbac.PlatformPrincipal;
import com.iflytek.skillhub.domain.user.UserStatus;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IdentityAuthenticatorTest {

    @Mock
    private AccessPolicy accessPolicy;

    @Mock
    private IdentityBindingService bindingService;

    private IdentityAuthenticator authenticator;

    @BeforeEach
    void setUp() {
        authenticator = new IdentityAuthenticator(accessPolicy, bindingService);
    }

    @Test
    void authenticate_allowDecision_delegatesToBindOrCreate() {
        IdentityClaims claims = new OAuthClaims("github", "gh_1", "u@example.com", true, "user", Map.of());
        PlatformPrincipal expected = new PlatformPrincipal("usr_1", "user", "u@example.com", null, "github", Set.of("USER"));

        when(accessPolicy.evaluate(claims)).thenReturn(AccessDecision.ALLOW);
        when(bindingService.bindOrCreate(claims, UserStatus.ACTIVE)).thenReturn(expected);

        PlatformPrincipal result = authenticator.authenticate(claims);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void authenticate_pendingDecision_createsPendingUserAndThrows() {
        IdentityClaims claims = new OAuthClaims("cas", "user-x", "x@example.com", true, "X", Map.of());

        when(accessPolicy.evaluate(claims)).thenReturn(AccessDecision.PENDING_APPROVAL);

        assertThatThrownBy(() -> authenticator.authenticate(claims))
            .isInstanceOf(AccountPendingException.class);

        verify(bindingService).createPendingUserIfAbsent(claims);
        verify(bindingService, never()).bindOrCreate(any(), any());
    }

    @Test
    void authenticate_denyDecision_throwsAccessDeniedByPolicy_andDoesNotBind() {
        IdentityClaims claims = new OAuthClaims("cas", "user-y", "y@bad.example", true, "Y", Map.of());

        when(accessPolicy.evaluate(claims)).thenReturn(AccessDecision.DENY);

        assertThatThrownBy(() -> authenticator.authenticate(claims))
            .isInstanceOf(AccessDeniedByPolicyException.class);

        verify(bindingService, never()).bindOrCreate(any(), any());
        verify(bindingService, never()).createPendingUserIfAbsent(any());
    }
}
