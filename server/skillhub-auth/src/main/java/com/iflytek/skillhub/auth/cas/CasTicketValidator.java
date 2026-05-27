package com.iflytek.skillhub.auth.cas;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Validates CAS tickets by calling the CAS server's serviceValidate endpoint.
 * Supports both CAS 2.0 (XML) and CAS 3.0 (JSON) protocols.
 */
@Component
public class CasTicketValidator {

    private static final Logger log = LoggerFactory.getLogger(CasTicketValidator.class);

    private final CasProperties casProperties;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public CasTicketValidator(CasProperties casProperties, ObjectMapper objectMapper) {
        this(casProperties, objectMapper, RestClient.builder().build());
    }

    CasTicketValidator(CasProperties casProperties, ObjectMapper objectMapper, RestClient restClient) {
        this.casProperties = casProperties;
        this.objectMapper = objectMapper;
        this.restClient = restClient;
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
        log.debug("Validating CAS ticket at: {}", validationUrl);

        try {
            String response = restClient.get()
                .uri(validationUrl)
                .retrieve()
                .body(String.class);

            if (response == null || response.isBlank()) {
                throw new CasValidationException("Empty response from CAS server");
            }

            if ("3.0".equals(casProperties.getProtocolVersion())) {
                return parseJsonResponse(response);
            } else {
                return parseXmlResponse(response);
            }
        } catch (CasValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("CAS ticket validation failed", e);
            throw new CasValidationException("Failed to validate CAS ticket: " + e.getMessage(), e);
        }
    }

    private String buildValidationUrl(String ticket) {
        String endpoint = "3.0".equals(casProperties.getProtocolVersion())
            ? "/p3/serviceValidate"
            : "/serviceValidate";

        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(casProperties.getServerUrl() + endpoint)
            .queryParam("ticket", ticket)
            .queryParam("service", casProperties.getServiceUrl());

        if ("3.0".equals(casProperties.getProtocolVersion())) {
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
                } else if (value.isArray() && value.size() > 0) {
                    attributes.put(entry.getKey(), value.get(0).asText());
                } else {
                    attributes.put(entry.getKey(), value.toString());
                }
            });
        }

        return extractClaims(user, attributes);
    }

    private CasIdentityClaims parseXmlResponse(String response) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(response.getBytes()));

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

        String subject = attributes.getOrDefault(usernameAttr, user).toString();
        String displayName = attributes.getOrDefault(displayNameAttr, user).toString();
        String email = attributes.containsKey(emailAttr) ? attributes.get(emailAttr).toString() : null;

        return new CasIdentityClaims(subject, email, displayName, attributes);
    }
}
