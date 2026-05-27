package com.iflytek.skillhub.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.iflytek.skillhub.auth.bootstrap.PassiveSessionAuthenticator;
import com.iflytek.skillhub.auth.cas.CasProperties;
import com.iflytek.skillhub.auth.direct.DirectAuthProvider;
import com.iflytek.skillhub.auth.direct.DirectAuthRequest;
import com.iflytek.skillhub.auth.rbac.PlatformPrincipal;
import com.iflytek.skillhub.config.AuthSessionBootstrapProperties;
import com.iflytek.skillhub.config.DirectAuthProperties;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;

class AuthMethodCatalogTest {

    @Test
    void listMethodsShouldUseProviderDisplayNamesForCompatibleAuthMethods() {
        OAuth2ClientProperties oauthProperties = new OAuth2ClientProperties();
        DirectAuthProperties directAuthProperties = new DirectAuthProperties();
        directAuthProperties.setEnabled(true);
        AuthSessionBootstrapProperties bootstrapProperties = new AuthSessionBootstrapProperties();
        bootstrapProperties.setEnabled(true);

        DirectAuthProvider directProvider = new DirectAuthProvider() {
            @Override
            public String providerCode() {
                return "private-sso";
            }

            @Override
            public String displayName() {
                return "Enterprise Password";
            }

            @Override
            public PlatformPrincipal authenticate(DirectAuthRequest request) {
                throw new UnsupportedOperationException("not used in catalog test");
            }
        };

        PassiveSessionAuthenticator bootstrapProvider = new PassiveSessionAuthenticator() {
            @Override
            public String providerCode() {
                return "private-sso";
            }

            @Override
            public String displayName() {
                return "Enterprise SSO";
            }

            @Override
            public Optional<PlatformPrincipal> authenticate(jakarta.servlet.http.HttpServletRequest request) {
                return Optional.empty();
            }
        };

        AuthMethodCatalog catalog = new AuthMethodCatalog(
            oauthProperties,
            directAuthProperties,
            bootstrapProperties,
            new CasProperties(),
            List.of(directProvider),
            List.of(bootstrapProvider)
        );

        assertThat(catalog.listMethods(null))
            .extracting(method -> method.id() + ":" + method.displayName())
            .contains(
                "local-password:Local Account",
                "direct-private-sso:Enterprise Password",
                "bootstrap-private-sso:Enterprise SSO"
            );
    }

    @Test
    void listMethodsShouldFallBackToProviderCodeWhenDisplayNameIsNotOverridden() {
        OAuth2ClientProperties oauthProperties = new OAuth2ClientProperties();
        DirectAuthProperties directAuthProperties = new DirectAuthProperties();
        directAuthProperties.setEnabled(true);
        AuthSessionBootstrapProperties bootstrapProperties = new AuthSessionBootstrapProperties();
        bootstrapProperties.setEnabled(true);

        DirectAuthProvider directProvider = new DirectAuthProvider() {
            @Override
            public String providerCode() {
                return "private-sso";
            }

            @Override
            public PlatformPrincipal authenticate(DirectAuthRequest request) {
                return mock(PlatformPrincipal.class);
            }
        };

        PassiveSessionAuthenticator bootstrapProvider = new PassiveSessionAuthenticator() {
            @Override
            public String providerCode() {
                return "private-sso";
            }

            @Override
            public Optional<PlatformPrincipal> authenticate(jakarta.servlet.http.HttpServletRequest request) {
                return Optional.empty();
            }
        };

        AuthMethodCatalog catalog = new AuthMethodCatalog(
            oauthProperties,
            directAuthProperties,
            bootstrapProperties,
            new CasProperties(),
            List.of(directProvider),
            List.of(bootstrapProvider)
        );

        assertThat(catalog.listMethods(null))
            .extracting(method -> method.id() + ":" + method.displayName())
            .contains(
                "direct-private-sso:private-sso",
                "bootstrap-private-sso:private-sso"
            );
    }

    @Test
    void listMethodsExposesCasWhenEnabled() {
        OAuth2ClientProperties oauthProperties = new OAuth2ClientProperties();
        DirectAuthProperties directAuthProperties = new DirectAuthProperties();
        AuthSessionBootstrapProperties bootstrapProperties = new AuthSessionBootstrapProperties();

        CasProperties casProperties = new CasProperties();
        casProperties.setEnabled(true);
        casProperties.setServerUrl("https://cas.example.com");
        casProperties.setServiceUrl("https://skillhub.example.com/api/v1/auth/cas/callback");
        casProperties.setProtocolVersion("3.0");
        casProperties.setAllowInsecureServer(true);
        casProperties.validate();

        AuthMethodCatalog catalog = new AuthMethodCatalog(
            oauthProperties,
            directAuthProperties,
            bootstrapProperties,
            casProperties,
            List.of(),
            List.of()
        );

        assertThat(catalog.listMethods(null))
            .extracting(method -> method.id() + ":" + method.methodType() + ":" + method.actionUrl())
            .contains("cas:CAS_REDIRECT:/api/v1/auth/cas/login");
    }

    @Test
    void listMethodsOmitsCasWhenDisabled() {
        OAuth2ClientProperties oauthProperties = new OAuth2ClientProperties();
        DirectAuthProperties directAuthProperties = new DirectAuthProperties();
        AuthSessionBootstrapProperties bootstrapProperties = new AuthSessionBootstrapProperties();

        AuthMethodCatalog catalog = new AuthMethodCatalog(
            oauthProperties,
            directAuthProperties,
            bootstrapProperties,
            new CasProperties(),
            List.of(),
            List.of()
        );

        assertThat(catalog.listMethods(null))
            .extracting(method -> method.id())
            .doesNotContain("cas");
    }
}
