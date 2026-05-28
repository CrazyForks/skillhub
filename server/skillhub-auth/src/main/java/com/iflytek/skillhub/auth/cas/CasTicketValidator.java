package com.iflytek.skillhub.auth.cas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Validates CAS tickets by calling the CAS server's serviceValidate endpoint.
 * Supports both CAS 2.0 (XML) and CAS 3.0 (JSON) protocols.
 */
@Component
public class CasTicketValidator {

    private static final Logger log = LoggerFactory.getLogger(CasTicketValidator.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final CasProperties casProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public CasTicketValidator(CasProperties casProperties, ObjectMapper objectMapper) {
        this(casProperties, objectMapper, defaultRestClient());
    }

    CasTicketValidator(CasProperties casProperties, ObjectMapper objectMapper, RestClient restClient) {
        this.casProperties = casProperties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
    }

    private static RestClient defaultRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(READ_TIMEOUT);
        return RestClient.builder().requestFactory(factory).build();
    }

    /**
     * Validates a CAS ticket and returns the user attributes.
     *
     * @param ticket the service ticket from CAS redirect
     * @return CasIdentityClaims with user attributes
     * @throws CasValidationException if validation fails
     */
    public CasIdentityClaims validate(String ticket) {
        if (!casProperties.isEnabled()) {
            throw new IllegalStateException("CAS authentication is not enabled");
        }

        String validationUrl = buildValidationUrl(ticket);
        log.debug("Validating CAS ticket at endpoint: {}", casProperties.resolvedProtocolVersion().validatePath());

        try {
            String response = restClient.get()
                .uri(validationUrl)
                .retrieve()
                .body(String.class);

            if (response == null || response.isBlank()) {
                throw new CasValidationException("Empty response from CAS server");
            }

            if (casProperties.resolvedProtocolVersion().isJson()) {
                return parseJsonResponse(response);
            } else {
                return parseXmlResponse(response);
            }
        } catch (CasValidationException e) {
            throw e;
        } catch (Exception e) {
            // Log full exception (including URL) at server level for operators; surface only the
            // exception class to callers so the ticket query parameter never reaches downstream
            // log streams or error redirects.
            log.error("CAS ticket validation request failed", e);
            throw new CasValidationException(
                "Failed to validate CAS ticket: " + e.getClass().getSimpleName(), e);
        }
    }

    private String buildValidationUrl(String ticket) {
        CasProtocolVersion version = casProperties.resolvedProtocolVersion();

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(casProperties.getServerUrl() + version.validatePath())
            .queryParam("ticket", ticket)
            .queryParam("service", casProperties.getServiceUrl());

        if (version.isJson()) {
            builder.queryParam("format", "JSON");
        }

        return builder.toUriString();
    }

    private CasIdentityClaims parseJsonResponse(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode serviceResponse = root.path("serviceResponse");

        if (serviceResponse.has("authenticationFailure")) {
            String code = serviceResponse.path("authenticationFailure").path("code").asText("UNKNOWN");
            String description = serviceResponse.path("authenticationFailure").path("description").asText("Unknown error");
            throw new CasValidationException("CAS authentication failed: " + code + " - " + description);
        }

        JsonNode authSuccess = serviceResponse.path("authenticationSuccess");
        if (authSuccess.isMissingNode()) {
            throw new CasValidationException("Invalid CAS response: missing authenticationSuccess");
        }

        String user = authSuccess.path("user").asText(null);
        if (user == null || user.isBlank()) {
            throw new CasValidationException("CAS response missing user identifier");
        }

        JsonNode attributesNode = authSuccess.path("attributes");
        Map<String, Object> attributes = new HashMap<>();
        if (attributesNode.isObject()) {
            attributesNode.fields().forEachRemaining(entry -> {
                JsonNode value = entry.getValue();
                if (value.isTextual()) {
                    attributes.put(entry.getKey(), value.asText());
                } else if (value.isArray()) {
                    if (value.size() == 1) {
                        attributes.put(entry.getKey(), value.get(0).asText());
                    } else if (value.size() > 1) {
                        java.util.List<String> values = new java.util.ArrayList<>(value.size());
                        for (JsonNode item : value) {
                            values.add(item.asText());
                        }
                        attributes.put(entry.getKey(), java.util.List.copyOf(values));
                    }
                } else if (value.isNumber() || value.isBoolean()) {
                    attributes.put(entry.getKey(), value.asText());
                } else if (!value.isNull()) {
                    attributes.put(entry.getKey(), value.toString());
                }
            });
        }

        return extractClaims(user, attributes);
    }

    private CasIdentityClaims parseXmlResponse(String response) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(response.getBytes(java.nio.charset.StandardCharsets.UTF_8)));

        Element root = doc.getDocumentElement();

        NodeList failures = root.getElementsByTagNameNS("*", "authenticationFailure");
        if (failures.getLength() > 0) {
            Element failure = (Element) failures.item(0);
            String code = failure.getAttribute("code");
            String description = failure.getTextContent();
            throw new CasValidationException("CAS authentication failed: " + code + " - " + description);
        }

        NodeList successNodes = root.getElementsByTagNameNS("*", "authenticationSuccess");
        if (successNodes.getLength() == 0) {
            throw new CasValidationException("Invalid CAS response: missing authenticationSuccess");
        }

        Element authSuccess = (Element) successNodes.item(0);
        NodeList userNodes = authSuccess.getElementsByTagNameNS("*", "user");
        if (userNodes.getLength() == 0) {
            throw new CasValidationException("CAS response missing user identifier");
        }

        String user = userNodes.item(0).getTextContent();
        if (user == null || user.isBlank()) {
            throw new CasValidationException("CAS response has blank user identifier");
        }

        Map<String, Object> attributes = new HashMap<>();
        NodeList attributesNodes = authSuccess.getElementsByTagNameNS("*", "attributes");
        if (attributesNodes.getLength() > 0) {
            Element attributesElement = (Element) attributesNodes.item(0);
            NodeList children = attributesElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i) instanceof Element) {
                    Element attr = (Element) children.item(i);
                    String localName = attr.getLocalName();
                    String value = attr.getTextContent();
                    if (localName != null && value != null) {
                        attributes.put(localName, value);
                    }
                }
            }
        }

        return extractClaims(user, attributes);
    }

    private CasIdentityClaims extractClaims(String user, Map<String, Object> attributes) {
        String usernameAttr = casProperties.getAttributes().get("username");
        String displayNameAttr = casProperties.getAttributes().get("display-name");
        String emailAttr = casProperties.getAttributes().get("email");

        String subject = firstStringOr(attributes.get(usernameAttr), user);
        String displayName = firstStringOr(attributes.get(displayNameAttr), user);
        String email = firstStringOr(attributes.get(emailAttr), null);

        return new CasIdentityClaims(subject, email, displayName, attributes);
    }

    private static String firstStringOr(Object value, String fallback) {
        if (value == null) {
            return fallback;
        }
        if (value instanceof java.util.List<?> list) {
            return list.isEmpty() ? fallback : String.valueOf(list.get(0));
        }
        return value.toString();
    }
}
