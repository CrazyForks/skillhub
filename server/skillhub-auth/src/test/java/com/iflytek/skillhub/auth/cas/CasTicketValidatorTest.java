package com.iflytek.skillhub.auth.cas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class CasTicketValidatorTest {

    private MockRestServiceServer mockServer;
    private CasProperties casProperties;
    private CasTicketValidator validator;

    @BeforeEach
    void setUp() {
        casProperties = newProperties("3.0");
        validator = newValidator(casProperties);
    }

    private CasProperties newProperties(String protocolVersion) {
        CasProperties props = new CasProperties();
        props.setEnabled(true);
        props.setServerUrl("https://cas.example.com");
        props.setServiceUrl("https://skillhub.example.com/api/v1/auth/cas/callback");
        props.setProtocolVersion(protocolVersion);
        props.setAllowInsecureServer(true);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("username", "uid");
        attributes.put("display-name", "cn");
        attributes.put("email", "mail");
        props.setAttributes(attributes);

        // Trigger @PostConstruct logic (resolvedProtocolVersion etc.)
        props.validate();
        return props;
    }

    private CasTicketValidator newValidator(CasProperties props) {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        return new CasTicketValidator(props, new ObjectMapper(), builder.build());
    }

    @Test
    void validate_cas30_json_success_buildsExactValidationUrl() {
        String jsonResponse = """
            {
              "serviceResponse": {
                "authenticationSuccess": {
                  "user": "zhangsan",
                  "attributes": {
                    "uid": "zhangsan",
                    "cn": "Zhang San",
                    "mail": "zhangsan@example.com",
                    "department": "Engineering"
                  }
                }
              }
            }
            """;

        mockServer.expect(requestTo(allOf(
            startsWith("https://cas.example.com/p3/serviceValidate"),
            containsString("ticket=ST-12345"),
            containsString("service=https"),
            containsString("format=JSON")
        ))).andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        CasIdentityClaims claims = validator.validate("ST-12345");

        assertThat(claims.subject()).isEqualTo("zhangsan");
        assertThat(claims.providerLogin()).isEqualTo("Zhang San");
        assertThat(claims.email()).isEqualTo("zhangsan@example.com");
        assertThat(claims.provider()).isEqualTo("cas");
        assertThat(claims.extra()).containsEntry("department", "Engineering");

        mockServer.verify();
    }

    @Test
    void validate_cas30_json_authenticationFailure() {
        String jsonResponse = """
            {
              "serviceResponse": {
                "authenticationFailure": {
                  "code": "INVALID_TICKET",
                  "description": "Ticket ST-expired has expired"
                }
              }
            }
            """;

        mockServer.expect(requestTo(containsString("/p3/serviceValidate")))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> validator.validate("ST-expired"))
            .isInstanceOf(CasValidationException.class)
            .hasMessageContaining("INVALID_TICKET");

        mockServer.verify();
    }

    @Test
    void validate_cas20_xml_success_buildsExactValidationUrl() {
        casProperties = newProperties("2.0");
        validator = newValidator(casProperties);

        String xmlResponse = """
            <cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">
              <cas:authenticationSuccess>
                <cas:user>lisi</cas:user>
                <cas:attributes>
                  <cas:uid>lisi</cas:uid>
                  <cas:cn>Li Si</cas:cn>
                  <cas:mail>lisi@example.com</cas:mail>
                </cas:attributes>
              </cas:authenticationSuccess>
            </cas:serviceResponse>
            """;

        mockServer.expect(requestTo(allOf(
            startsWith("https://cas.example.com/serviceValidate"),
            not(containsString("/p3/")),
            containsString("ticket=ST-67890"),
            containsString("service=https"),
            not(containsString("format=JSON"))
        ))).andRespond(withSuccess(xmlResponse, MediaType.APPLICATION_XML));

        CasIdentityClaims claims = validator.validate("ST-67890");

        assertThat(claims.subject()).isEqualTo("lisi");
        assertThat(claims.providerLogin()).isEqualTo("Li Si");
        assertThat(claims.email()).isEqualTo("lisi@example.com");
        assertThat(claims.provider()).isEqualTo("cas");

        mockServer.verify();
    }

    @Test
    void validate_cas20_xml_authenticationFailure() {
        casProperties = newProperties("2.0");
        validator = newValidator(casProperties);

        String xmlResponse = """
            <cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">
              <cas:authenticationFailure code="INVALID_SERVICE">
                Service not recognized
              </cas:authenticationFailure>
            </cas:serviceResponse>
            """;

        mockServer.expect(requestTo(containsString("/serviceValidate")))
            .andRespond(withSuccess(xmlResponse, MediaType.APPLICATION_XML));

        assertThatThrownBy(() -> validator.validate("ST-bad"))
            .isInstanceOf(CasValidationException.class)
            .hasMessageContaining("INVALID_SERVICE");

        mockServer.verify();
    }

    @Test
    void validate_cas30_json_missingUser() {
        String jsonResponse = """
            {
              "serviceResponse": {
                "authenticationSuccess": {
                  "attributes": {
                    "uid": "someone"
                  }
                }
              }
            }
            """;

        mockServer.expect(requestTo(containsString("/p3/serviceValidate")))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> validator.validate("ST-nouser"))
            .isInstanceOf(CasValidationException.class)
            .hasMessageContaining("missing user identifier");

        mockServer.verify();
    }

    @Test
    void validate_whenDisabled_throwsIllegalState() {
        casProperties.setEnabled(false);

        assertThatThrownBy(() -> validator.validate("ST-any"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not enabled");
    }

    @Test
    void validate_cas30_json_singleArrayAttribute_unwrapped() {
        String jsonResponse = """
            {
              "serviceResponse": {
                "authenticationSuccess": {
                  "user": "wangwu",
                  "attributes": {
                    "uid": ["wangwu"],
                    "cn": ["Wang Wu"],
                    "mail": ["wangwu@example.com"]
                  }
                }
              }
            }
            """;

        mockServer.expect(requestTo(containsString("/p3/serviceValidate")))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        CasIdentityClaims claims = validator.validate("ST-array1");

        assertThat(claims.subject()).isEqualTo("wangwu");
        assertThat(claims.providerLogin()).isEqualTo("Wang Wu");
        assertThat(claims.email()).isEqualTo("wangwu@example.com");

        mockServer.verify();
    }

    @Test
    void validate_cas30_json_multiValueArray_preservedAsList() {
        String jsonResponse = """
            {
              "serviceResponse": {
                "authenticationSuccess": {
                  "user": "wangwu",
                  "attributes": {
                    "uid": "wangwu",
                    "cn": "Wang Wu",
                    "memberOf": ["group1", "group2", "group3"]
                  }
                }
              }
            }
            """;

        mockServer.expect(requestTo(containsString("/p3/serviceValidate")))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        CasIdentityClaims claims = validator.validate("ST-multi");

        assertThat(claims.extra()).containsKey("memberOf");
        assertThat(claims.extra().get("memberOf")).isInstanceOf(List.class);
        @SuppressWarnings("unchecked")
        List<String> memberOf = (List<String>) claims.extra().get("memberOf");
        assertThat(memberOf).containsExactly("group1", "group2", "group3");

        mockServer.verify();
    }

    @Test
    void validate_fallsBackToUserWhenAttributesMissing() {
        String jsonResponse = """
            {
              "serviceResponse": {
                "authenticationSuccess": {
                  "user": "fallback-user",
                  "attributes": {}
                }
              }
            }
            """;

        mockServer.expect(requestTo(containsString("/p3/serviceValidate")))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        CasIdentityClaims claims = validator.validate("ST-noattrs");

        assertThat(claims.subject()).isEqualTo("fallback-user");
        assertThat(claims.providerLogin()).isEqualTo("fallback-user");
        assertThat(claims.email()).isNull();

        mockServer.verify();
    }

    @Test
    void validate_cas20_xml_xxePayload_isRejected() {
        casProperties = newProperties("2.0");
        validator = newValidator(casProperties);

        // XXE attempt: external entity referencing a local file. With XXE hardening enabled, the
        // parser must refuse the DOCTYPE outright (or refuse to resolve the entity); either way
        // the local file content must NOT appear in the parsed user attribute.
        String xxePayload = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE serviceResponse [
              <!ENTITY xxe SYSTEM "file:///etc/passwd">
            ]>
            <cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">
              <cas:authenticationSuccess>
                <cas:user>&xxe;</cas:user>
                <cas:attributes>
                  <cas:uid>&xxe;</cas:uid>
                </cas:attributes>
              </cas:authenticationSuccess>
            </cas:serviceResponse>
            """;

        mockServer.expect(requestTo(containsString("/serviceValidate")))
            .andRespond(withSuccess(xxePayload, MediaType.APPLICATION_XML));

        assertThatThrownBy(() -> validator.validate("ST-xxe"))
            .isInstanceOf(CasValidationException.class)
            .satisfies(e -> {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                assertThat(msg).doesNotContain("root:");
                assertThat(msg).doesNotContain("/bin/bash");
            })
            .cause()
            .satisfies(cause -> {
                assertThat(cause.getClass().getName()).contains("SAXParseException");
                assertThat(cause.getMessage()).containsIgnoringCase("DOCTYPE");
            });
    }

    @Test
    void validate_cas20_xml_billionLaughs_isRejected() {
        casProperties = newProperties("2.0");
        validator = newValidator(casProperties);

        String billionLaughs = """
            <?xml version="1.0"?>
            <!DOCTYPE lolz [
              <!ENTITY lol "lol">
              <!ENTITY lol2 "&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;&lol;">
              <!ENTITY lol3 "&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;&lol2;">
            ]>
            <cas:serviceResponse xmlns:cas="http://www.yale.edu/tp/cas">
              <cas:authenticationSuccess>
                <cas:user>&lol3;</cas:user>
              </cas:authenticationSuccess>
            </cas:serviceResponse>
            """;

        mockServer.expect(requestTo(containsString("/serviceValidate")))
            .andRespond(withSuccess(billionLaughs, MediaType.APPLICATION_XML));

        // disallow-doctype-decl=true means the parser refuses any DOCTYPE; we expect a validation
        // exception wrapping a SAXParseException that rejects the DOCTYPE declaration.
        assertThatThrownBy(() -> validator.validate("ST-laugh"))
            .isInstanceOf(CasValidationException.class)
            .cause()
            .satisfies(cause -> {
                assertThat(cause.getClass().getName()).contains("SAXParseException");
                assertThat(cause.getMessage()).containsIgnoringCase("DOCTYPE");
            });
    }
}
