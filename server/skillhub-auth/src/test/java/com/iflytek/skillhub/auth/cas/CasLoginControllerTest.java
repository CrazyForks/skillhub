package com.iflytek.skillhub.auth.cas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iflytek.skillhub.auth.identity.AccessDeniedByPolicyException;
import com.iflytek.skillhub.auth.identity.AccountDisabledException;
import com.iflytek.skillhub.auth.identity.AccountPendingException;
import com.iflytek.skillhub.auth.identity.IdentityAuthenticator;
import com.iflytek.skillhub.auth.rbac.PlatformPrincipal;
import com.iflytek.skillhub.auth.session.PlatformSessionService;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;

@ExtendWith(MockitoExtension.class)
class CasLoginControllerTest {

    @Mock
    private CasTicketValidator ticketValidator;

    @Mock
    private IdentityAuthenticator identityAuthenticator;

    @Mock
    private PlatformSessionService sessionService;

    private CasProperties casProperties;
    private CasLoginController controller;

    @BeforeEach
    void setUp() {
        casProperties = new CasProperties();
        casProperties.setEnabled(true);
        casProperties.setServerUrl("https://cas.example.com");
        casProperties.setServiceUrl("https://skillhub.example.com/api/v1/auth/cas/callback");
        casProperties.setProtocolVersion("3.0");
        casProperties.setAllowInsecureServer(true);

        controller = new CasLoginController(casProperties, ticketValidator, identityAuthenticator, sessionService);
    }

    // ─── login() ───────────────────────────────────────────────────────────────

    @Test
    void login_redirectsToCasServer() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.login(null, request);

        assertThat(result).startsWith("redirect:https://cas.example.com/login");
        assertThat(result).contains("service=");
    }

    @Test
    void login_storesCasReturnToInSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        controller.login("/skills", request);

        assertThat(request.getSession(false))
            .isNotNull()
            .extracting(s -> s.getAttribute(CasLoginController.SESSION_CAS_RETURN_TO_ATTRIBUTE))
            .isEqualTo("/skills");
        // Must NOT bleed into the shared OAuth key
        assertThat(request.getSession(false)
            .getAttribute("skillhub.oauth.returnTo")).isNull();
    }

    @Test
    void login_storesStateNonceInSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        controller.login(null, request);

        Object nonce = request.getSession(false)
            .getAttribute(CasLoginController.SESSION_CAS_STATE_ATTRIBUTE);
        assertThat(nonce).isInstanceOf(String.class);
        assertThat((String) nonce).hasSizeGreaterThanOrEqualTo(20);
    }

    @Test
    void login_embedsStateInServiceUrl() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.login(null, request);

        String nonce = (String) request.getSession(false)
            .getAttribute(CasLoginController.SESSION_CAS_STATE_ATTRIBUTE);
        // The service URL is percent-encoded when embedded as a query param in the CAS login URL.
        // The state param itself may appear percent-encoded (= → %3D) or raw depending on how
        // UriComponentsBuilder encodes the outer service param. Assert the nonce value appears
        // in the redirect URL in either form.
        assertThat(result).satisfiesAnyOf(
            r -> assertThat(r).contains("state=" + nonce),
            r -> assertThat(r).contains("state%3D" + nonce),
            r -> assertThat(r).contains("state%3d" + nonce)
        );
    }

    @Test
    void login_rejectsInvalidReturnTo() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        controller.login("https://evil.com", request);

        assertThat(request.getSession(false)
            .getAttribute(CasLoginController.SESSION_CAS_RETURN_TO_ATTRIBUTE)).isNull();
    }

    @Test
    void login_whenDisabled_redirectsWithError() {
        casProperties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.login(null, request);

        assertThat(result).isEqualTo("redirect:/login?error=cas_disabled");
    }

    // ─── callback() ────────────────────────────────────────────────────────────

    private MockHttpServletRequest requestWithValidState(String returnTo) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CasLoginController.SESSION_CAS_STATE_ATTRIBUTE, "valid-nonce");
        if (returnTo != null) {
            session.setAttribute(CasLoginController.SESSION_CAS_RETURN_TO_ATTRIBUTE, returnTo);
        }
        request.setSession(session);
        return request;
    }

    @Test
    void callback_successfulTicketValidation() {
        MockHttpServletRequest request = requestWithValidState("/dashboard");

        CasIdentityClaims claims = new CasIdentityClaims("zhangsan", "zhangsan@example.com", "Zhang San", Map.of());
        PlatformPrincipal principal = new PlatformPrincipal("usr_123", "Zhang San", "zhangsan@example.com", null, "cas", Set.of("USER"));

        when(ticketValidator.validate("ST-12345")).thenReturn(claims);
        when(identityAuthenticator.authenticate(claims)).thenReturn(principal);

        String result = controller.callback("ST-12345", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/dashboard");
        verify(sessionService).establishSession(eq(principal), eq(request));
    }

    @Test
    void callback_usesDefaultTargetWhenNoReturnTo() {
        MockHttpServletRequest request = requestWithValidState(null);

        CasIdentityClaims claims = new CasIdentityClaims("user1", null, "User One", Map.of());
        PlatformPrincipal principal = new PlatformPrincipal("usr_456", "User One", null, null, "cas", Set.of("USER"));

        when(ticketValidator.validate("ST-99999")).thenReturn(claims);
        when(identityAuthenticator.authenticate(claims)).thenReturn(principal);

        String result = controller.callback("ST-99999", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/dashboard");
    }

    @Test
    void callback_sanitizesUnsafeReturnTo() {
        MockHttpServletRequest request = requestWithValidState("https://evil.example/steal");

        CasIdentityClaims claims = new CasIdentityClaims("u", null, "U", Map.of());
        PlatformPrincipal principal = new PlatformPrincipal("usr_x", "U", null, null, "cas", Set.of("USER"));

        when(ticketValidator.validate("ST-evil")).thenReturn(claims);
        when(identityAuthenticator.authenticate(claims)).thenReturn(principal);

        String result = controller.callback("ST-evil", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/dashboard");
    }

    @Test
    void callback_missingTicket_redirectsWithError() {
        MockHttpServletRequest request = requestWithValidState(null);

        String result = controller.callback(null, "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/login?error=missing_ticket");
        verify(ticketValidator, never()).validate(any());
    }

    @Test
    void callback_blankTicket_redirectsWithError() {
        MockHttpServletRequest request = requestWithValidState(null);

        String result = controller.callback("  ", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/login?error=missing_ticket");
    }

    @Test
    void callback_whenDisabled_redirectsWithError() {
        casProperties.setEnabled(false);
        MockHttpServletRequest request = requestWithValidState(null);

        String result = controller.callback("ST-12345", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/login?error=cas_disabled");
    }

    @Test
    void callback_missingState_rejectsCsrf() {
        MockHttpServletRequest request = requestWithValidState(null);

        String result = controller.callback("ST-csrf", null, request);

        assertThat(result).isEqualTo("redirect:/login?error=invalid_state");
        verify(ticketValidator, never()).validate(any());
    }

    @Test
    void callback_wrongState_rejectsCsrf() {
        MockHttpServletRequest request = requestWithValidState(null);

        String result = controller.callback("ST-csrf", "wrong-nonce", request);

        assertThat(result).isEqualTo("redirect:/login?error=invalid_state");
        verify(ticketValidator, never()).validate(any());
    }

    @Test
    void callback_noSession_rejectsCsrf() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.callback("ST-csrf", "any-state", request);

        assertThat(result).isEqualTo("redirect:/login?error=invalid_state");
        verify(ticketValidator, never()).validate(any());
    }

    @Test
    void callback_accountPending_redirectsToPendingApproval() {
        MockHttpServletRequest request = requestWithValidState(null);

        CasIdentityClaims claims = new CasIdentityClaims("pending-user", null, "Pending", Map.of());
        when(ticketValidator.validate("ST-pending")).thenReturn(claims);
        when(identityAuthenticator.authenticate(claims)).thenThrow(new AccountPendingException());

        String result = controller.callback("ST-pending", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/pending-approval");
    }

    @Test
    void callback_accountDisabled_redirectsToAccessDenied() {
        MockHttpServletRequest request = requestWithValidState(null);

        CasIdentityClaims claims = new CasIdentityClaims("disabled-user", null, "Disabled", Map.of());
        when(ticketValidator.validate("ST-disabled")).thenReturn(claims);
        when(identityAuthenticator.authenticate(claims)).thenThrow(new AccountDisabledException());

        String result = controller.callback("ST-disabled", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/access-denied");
    }

    @Test
    void callback_accessPolicyDeny_redirectsToAccessDenied() {
        MockHttpServletRequest request = requestWithValidState(null);

        CasIdentityClaims claims = new CasIdentityClaims("denied-user", "denied@bad.example", "Denied", Map.of());
        when(ticketValidator.validate("ST-denied")).thenReturn(claims);
        when(identityAuthenticator.authenticate(claims)).thenThrow(new AccessDeniedByPolicyException());

        String result = controller.callback("ST-denied", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/access-denied");
    }

    @Test
    void callback_validationFailed_redirectsWithError() {
        MockHttpServletRequest request = requestWithValidState(null);

        when(ticketValidator.validate("ST-invalid")).thenThrow(new CasValidationException("Invalid ticket"));

        String result = controller.callback("ST-invalid", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/login?error=cas_validation_failed");
    }

    @Test
    void callback_unexpectedError_redirectsWithInternalError() {
        MockHttpServletRequest request = requestWithValidState(null);

        CasIdentityClaims claims = new CasIdentityClaims("user", null, "User", Map.of());
        when(ticketValidator.validate("ST-broken")).thenReturn(claims);
        when(identityAuthenticator.authenticate(claims)).thenThrow(new RuntimeException("DB down"));

        String result = controller.callback("ST-broken", "valid-nonce", request);

        assertThat(result).isEqualTo("redirect:/login?error=internal_error");
    }
}
