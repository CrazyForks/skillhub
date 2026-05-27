package com.iflytek.skillhub.auth.cas;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for CAS SSO integration.
 */
@Component
@ConfigurationProperties(prefix = "skillhub.auth.cas")
public class CasProperties {

    private boolean enabled = false;
    private String serverUrl;
    private String serviceUrl;
    private String protocolVersion = "3.0";
    private boolean allowInsecureServer = false;
    private Map<String, String> attributes = new HashMap<>();
    private CasProtocolVersion resolvedProtocolVersion = CasProtocolVersion.V3_0;

    @PostConstruct
    public void validate() {
        if (!enabled) {
            return;
        }

        if (serverUrl == null || serverUrl.isBlank()) {
            throw new IllegalStateException("skillhub.auth.cas.server-url must be configured when CAS is enabled");
        }

        if (serviceUrl == null || serviceUrl.isBlank()) {
            throw new IllegalStateException("skillhub.auth.cas.service-url must be configured when CAS is enabled");
        }

        if (!allowInsecureServer && !serverUrl.startsWith("https://")) {
            throw new IllegalStateException(
                "CAS server URL must use HTTPS in production. " +
                "Set skillhub.auth.cas.allow-insecure-server=true to override for development."
            );
        }

        if (!allowInsecureServer && !serviceUrl.startsWith("https://")) {
            throw new IllegalStateException(
                "CAS service URL must use HTTPS in production (otherwise the service ticket " +
                "is transmitted in plaintext). " +
                "Set skillhub.auth.cas.allow-insecure-server=true to override for development."
            );
        }

        try {
            this.resolvedProtocolVersion = CasProtocolVersion.from(protocolVersion);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "skillhub.auth.cas.protocol-version must be either '2.0' or '3.0'", e);
        }

        if (attributes.get("username") == null || attributes.get("username").isBlank()) {
            attributes.put("username", "uid");
        }
        if (attributes.get("display-name") == null || attributes.get("display-name").isBlank()) {
            attributes.put("display-name", "cn");
        }
        if (attributes.get("email") == null || attributes.get("email").isBlank()) {
            attributes.put("email", "mail");
        }
    }

    /**
     * Resolved protocol version after validation. Use this in the call path instead of
     * {@link #getProtocolVersion()} string comparisons.
     */
    public CasProtocolVersion resolvedProtocolVersion() {
        return resolvedProtocolVersion;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public boolean isAllowInsecureServer() {
        return allowInsecureServer;
    }

    public void setAllowInsecureServer(boolean allowInsecureServer) {
        this.allowInsecureServer = allowInsecureServer;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
