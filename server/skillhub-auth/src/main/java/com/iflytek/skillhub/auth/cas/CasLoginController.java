package com.iflytek.skillhub.auth.cas;

import com.iflytek.skillhub.auth.identity.AccessDeniedByPolicyException;
import com.iflytek.skillhub.auth.identity.AccountDisabledException;
import com.iflytek.skillhub.auth.identity.AccountPendingException;
import com.iflytek.skillhub.auth.identity.IdentityAuthenticator;
import com.iflytek.skillhub.auth.oauth.OAuthLoginRedirectSupport;
import com.iflytek.skillhub.auth.rbac.PlatformPrincipal;
import com.iflytek.skillhub.auth.session.PlatformSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Handles CAS SSO login flow: redirect to CAS server and callback with ticket validation.
 * Delegates access-policy evaluation and principal provisioning to {@link IdentityAuthenticator}
 * so the same allow/deny/pending decisions apply to OAuth and CAS uniformly.
 *
 * CSRF protection: login() stores a random nonce in the session and passes it as the
 * {@code state} parameter to the CAS login URL. callback() validates the nonce before
 * processing the ticket, preventing attackers from forcing a victim into an authenticated
 * session by crafting a callback URL with an attacker-controlled ticket.
 */
@Controller
@RequestMapping("/api/v1/auth/cas")
public class CasLoginController {

    private static final Logger log = LoggerFactory.getLogger(CasLoginController.class);

    static final String SESSION_CAS_RETURN_TO_ATTRIBUTE = "skillhub.cas.returnTo";
    static final String SESSION_CAS_STATE_ATTRIBUTE = "skillhub.cas.state";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final CasProperties casProperties;
    private final CasTicketValidator ticketValidator;
    private final IdentityAuthenticator identityAuthenticator;
    private final PlatformSessionService sessionService;

    public CasLoginController(
        CasProperties casProperties,
        CasTicketValidator ticketValidator,
        IdentityAuthenticator identityAuthenticator,
        PlatformSessionService sessionService
    ) {
        this.casProperties = casProperties;
        this.ticketValidator = ticketValidator;
        this.identityAuthenticator = identityAuthenticator;
        this.sessionService = sessionService;
    }

    @GetMapping("/login")
    public String login(
        @RequestParam(required = false) String returnTo,
        HttpServletRequest request
    ) {
        if (!casProperties.isEnabled()) {
            log.warn("CAS login attempted but CAS is not enabled");
            return "redirect:/login?error=cas_disabled";
        }

        HttpSession session = request.getSession(true);

        String sanitized = OAuthLoginRedirectSupport.sanitizeReturnTo(returnTo);
        if (sanitized != null) {
            session.setAttribute(SESSION_CAS_RETURN_TO_ATTRIBUTE, sanitized);
        } else {
            session.removeAttribute(SESSION_CAS_RETURN_TO_ATTRIBUTE);
        }

        String state = generateState();
        session.setAttribute(SESSION_CAS_STATE_ATTRIBUTE, state);

        String serviceUrl = casProperties.getServiceUrl() + "?state=" + state;

        String casLoginUrl = UriComponentsBuilder
            .fromHttpUrl(casProperties.getServerUrl() + "/login")
            .queryParam("service", serviceUrl)
            .toUriString();

        log.debug("Redirecting to CAS login: endpoint={}", casProperties.getServerUrl());
        return "redirect:" + casLoginUrl;
    }

    @GetMapping("/callback")
    public String callback(
        @RequestParam(required = false) String ticket,
        @RequestParam(required = false) String state,
        HttpServletRequest request
    ) {
        if (!casProperties.isEnabled()) {
            log.warn("CAS callback received but CAS is not enabled");
            return "redirect:/login?error=cas_disabled";
        }

        if (ticket == null || ticket.isBlank()) {
            log.warn("CAS callback received without ticket parameter");
            return "redirect:/login?error=missing_ticket";
        }

        // CSRF: validate state nonce before touching the ticket
        HttpSession session = request.getSession(false);
        if (!isValidState(session, state)) {
            log.warn("CAS callback state mismatch — possible CSRF attempt");
            return "redirect:/login?error=invalid_state";
        }
        assert session != null; // guaranteed by isValidState
        session.removeAttribute(SESSION_CAS_STATE_ATTRIBUTE);

        CasIdentityClaims claims;
        try {
            claims = ticketValidator.validate(ticket);
        } catch (CasValidationException e) {
            log.error("CAS ticket validation failed: {}", e.getMessage());
            return "redirect:/login?error=cas_validation_failed";
        }

        log.info("CAS ticket validated for subject={}", claims.subject());

        try {
            PlatformPrincipal principal = identityAuthenticator.authenticate(claims);
            sessionService.establishSession(principal, request);

            // Read returnTo from session AFTER establishSession (which may rotate the session id
            // but preserves attributes on the same session object)
            HttpSession postAuthSession = request.getSession(false);
            String returnTo = null;
            if (postAuthSession != null) {
                returnTo = (String) postAuthSession.getAttribute(SESSION_CAS_RETURN_TO_ATTRIBUTE);
                postAuthSession.removeAttribute(SESSION_CAS_RETURN_TO_ATTRIBUTE);
            }

            String targetUrl = OAuthLoginRedirectSupport.sanitizeReturnTo(returnTo);
            if (targetUrl == null) {
                targetUrl = OAuthLoginRedirectSupport.DEFAULT_TARGET_URL;
            }
            log.debug("CAS login successful, redirecting to: {}", targetUrl);
            return "redirect:" + targetUrl;

        } catch (AccountPendingException e) {
            log.warn("CAS user pending approval: subject={}", claims.subject());
            return "redirect:/pending-approval";
        } catch (AccountDisabledException e) {
            log.warn("CAS user disabled: subject={}", claims.subject());
            return "redirect:/access-denied";
        } catch (AccessDeniedByPolicyException e) {
            log.warn("CAS user denied by policy: subject={}", claims.subject());
            return "redirect:/access-denied";
        } catch (Exception e) {
            log.error("Unexpected error during CAS callback for subject={}", claims.subject(), e);
            return "redirect:/login?error=internal_error";
        }
    }

    private static String generateState() {
        byte[] bytes = new byte[24];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static boolean isValidState(HttpSession session, String incomingState) {
        if (session == null || incomingState == null || incomingState.isBlank()) {
            return false;
        }
        Object stored = session.getAttribute(SESSION_CAS_STATE_ATTRIBUTE);
        return stored instanceof String storedState && storedState.equals(incomingState);
    }
}
