package com.iflytek.skillhub.auth.cas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iflytek.skillhub.auth.identity.IdentityBindingService;
import com.iflytek.skillhub.auth.oauth.AccountDisabledException;
import com.iflytek.skillhub.auth.oauth.AccountPendingException;
import com.iflytek.skillhub.auth.rbac.PlatformPrincipal;
import com.iflytek.skillhub.auth.session.PlatformSessionService;
import com.iflytek.skillhub.domain.user.UserStatus;
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
    private IdentityBindingService identityBindingService;

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

        controller = new CasLoginController(casProperties, ticketValidator, identityBindingService, sessionService);
    }

    @Test
    void login_redirectsToCasServer() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.login(null, request);

        assertThat(result).startsWith("redirect:https://cas.example.com/login");
        assertThat(result).contains("service=");
    }

    @Test
    void login_storesReturnToInSession() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        controller.login("/skills", request);

        assertThat(request.getSession().getAttribute("skillhub.oauth.returnTo")).isEqualTo("/skills");
    }

    @Test
    void login_rejectsInvalidReturnTo() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        controller.login("https://evil.com", request);

        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void login_whenDisabled_redirectsWithError() {
        casProperties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.login(null, request);

        assertThat(result).isEqualTo("redirect:/login?error=cas_disabled");
    }

    @Test
    void callback_successfulTicketValidation() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("skillhub.oauth.returnTo", "/dashboard");
        request.setSession(session);

        CasIdentityClaims claims = new CasIdentityClaims("zhangsan", "zhangsan@example.com", "Zhang San", Map.of());
        PlatformPrincipal principal = new PlatformPrincipal("usr_123", "Zhang San", "zhangsan@example.com", null, "cas", Set.of("USER"));

        when(ticketValidator.validate("ST-12345")).thenReturn(claims);
        when(identityBindingService.bindOrCreate(claims, UserStatus.ACTIVE)).thenReturn(principal);

        String result = controller.callback("ST-12345", request);

        assertThat(result).isEqualTo("redirect:/dashboard");
        verify(sessionService).establishSession(eq(principal), eq(request));
    }

    @Test
    void callback_usesDefaultTargetWhenNoReturnTo() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        CasIdentityClaims claims = new CasIdentityClaims("user1", null, "User One", Map.of());
        PlatformPrincipal principal = new PlatformPrincipal("usr_456", "User One", null, null, "cas", Set.of("USER"));

        when(ticketValidator.validate("ST-99999")).thenReturn(claims);
        when(identityBindingService.bindOrCreate(claims, UserStatus.ACTIVE)).thenReturn(principal);

        String result = controller.callback("ST-99999", request);

        assertThat(result).isEqualTo("redirect:/dashboard");
    }

    @Test
    void callback_missingTicket_redirectsWithError() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.callback(null, request);

        assertThat(result).isEqualTo("redirect:/login?error=missing_ticket");
        verify(ticketValidator, never()).validate(any());
    }

    @Test
    void callback_blankTicket_redirectsWithError() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.callback("  ", request);

        assertThat(result).isEqualTo("redirect:/login?error=missing_ticket");
    }

    @Test
    void callback_whenDisabled_redirectsWithError() {
        casProperties.setEnabled(false);
        MockHttpServletRequest request = new MockHttpServletRequest();

        String result = controller.callback("ST-12345", request);

        assertThat(result).isEqualTo("redirect:/login?error=cas_disabled");
    }

    @Test
    void callback_accountPending_redirectsToPendingApproval() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        CasIdentityClaims claims = new CasIdentityClaims("pending-user", null, "Pending", Map.of());
        when(ticketValidator.validate("ST-pending")).thenReturn(claims);
        when(identityBindingService.bindOrCreate(claims, UserStatus.ACTIVE)).thenThrow(new AccountPendingException());

        String result = controller.callback("ST-pending", request);

        assertThat(result).isEqualTo("redirect:/pending-approval");
    }

    @Test
    void callback_accountDisabled_redirectsToAccessDenied() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        CasIdentityClaims claims = new CasIdentityClaims("disabled-user", null, "Disabled", Map.of());
        when(ticketValidator.validate("ST-disabled")).thenReturn(claims);
        when(identityBindingService.bindOrCreate(claims, UserStatus.ACTIVE)).thenThrow(new AccountDisabledException());

        String result = controller.callback("ST-disabled", request);

        assertThat(result).isEqualTo("redirect:/access-denied");
    }

    @Test
    void callback_validationFailed_redirectsWithError() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        when(ticketValidator.validate("ST-invalid")).thenThrow(new CasValidationException("Invalid ticket"));

        String result = controller.callback("ST-invalid", request);

        assertThat(result).isEqualTo("redirect:/login?error=cas_validation_failed");
    }
}
