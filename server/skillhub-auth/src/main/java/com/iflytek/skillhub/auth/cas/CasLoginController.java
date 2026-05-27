package com.iflytek.skillhub.auth.cas;

import com.iflytek.skillhub.auth.identity.AccessDeniedByPolicyException;
import com.iflytek.skillhub.auth.identity.IdentityAuthenticator;
import com.iflytek.skillhub.auth.oauth.AccountDisabledException;
import com.iflytek.skillhub.auth.oauth.AccountPendingException;
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

/**
 * Handles CAS SSO login flow: redirect to CAS server and callback with ticket validation.
 * Delegates access-policy evaluation and principal provisioning to {@link IdentityAuthenticator}
 * so the same allow/deny/pending decisions apply to OAuth and CAS uniformly.
 */
@Controller
@RequestMapping("/api/v1/auth/cas")
public class CasLoginController {

    private static final Logger log = LoggerFactory.getLogger(CasLoginController.class);

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

        String sanitized = OAuthLoginRedirectSupport.sanitizeReturnTo(returnTo);
        if (sanitized != null) {
            HttpSession session = request.getSession(true);
            session.setAttribute(OAuthLoginRedirectSupport.SESSION_RETURN_TO_ATTRIBUTE, sanitized);
        }

        String casLoginUrl = UriComponentsBuilder
            .fromHttpUrl(casProperties.getServerUrl() + "/login")
            .queryParam("service", casProperties.getServiceUrl())
            .toUriString();

        log.debug("Redirecting to CAS login: {}", casLoginUrl);
        return "redirect:" + casLoginUrl;
    }

    @GetMapping("/callback")
    public String callback(
        @RequestParam(required = false) String ticket,
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

            HttpSession session = request.getSession(false);
            String returnTo = null;
            if (session != null) {
                returnTo = (String) session.getAttribute(OAuthLoginRedirectSupport.SESSION_RETURN_TO_ATTRIBUTE);
                session.removeAttribute(OAuthLoginRedirectSupport.SESSION_RETURN_TO_ATTRIBUTE);
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
}
