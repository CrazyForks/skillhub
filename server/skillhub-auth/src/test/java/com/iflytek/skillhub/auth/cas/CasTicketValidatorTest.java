package com.iflytek.skillhub.auth.cas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestToUriTemplate;
import static org.hamcrest.Matchers.containsString;

class CasTicketValidatorTest {

    private MockRestServiceServer mockServer;
    private CasProperties casProperties;
    private CasTicketValidator validator;

    @BeforeEach
    void setUp() {
        casProperties = new CasProperties();
        casProperties.setEnabled(true);
        casProperties.setServerUrl("https://cas.example.com");
        casProperties.setServiceUrl("https://skillhub.example.com/api/v1/auth/cas/callback");
        casProperties.setProtocolVersion("3.0");
        casProperties.setAllowInsecureServer(true);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("username", "uid");
        attributes.put("display-name", "cn");
        attributes.put("email", "mail");
        casProperties.setAttributes(attributes);

        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        validator = new CasTicketValidator(casProperties, new ObjectMapper(), restClient);
    }

    @Test
    void validate_cas30_json_success() {
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

        mockServer.expect(requestTo(containsString("/p3/serviceValidate")))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

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
    void validate_cas20_xml_success() {
        casProperties.setProtocolVersion("2.0");
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        validator = new CasTicketValidator(casProperties, new ObjectMapper(), builder.build());

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

        mockServer.expect(requestTo(containsString("/serviceValidate")))
            .andRespond(withSuccess(xmlResponse, MediaType.APPLICATION_XML));

        CasIdentityClaims claims = validator.validate("ST-67890");

        assertThat(claims.subject()).isEqualTo("lisi");
        assertThat(claims.providerLogin()).isEqualTo("Li Si");
        assertThat(claims.email()).isEqualTo("lisi@example.com");
        assertThat(claims.provider()).isEqualTo("cas");

        mockServer.verify();
    }

    @Test
    void validate_cas20_xml_authenticationFailure() {
        casProperties.setProtocolVersion("2.0");
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        validator = new CasTicketValidator(casProperties, new ObjectMapper(), builder.build());

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
    void validate_cas30_json_arrayAttributes() {
        String jsonResponse = """
            {
              "serviceResponse": {
                "authenticationSuccess": {
                  "user": "wangwu",
                  "attributes": {
                    "uid": ["wangwu"],
                    "cn": ["Wang Wu"],
                    "mail": ["wangwu@example.com"],
                    "memberOf": ["group1", "group2"]
                  }
                }
              }
            }
            """;

        mockServer.expect(requestTo(containsString("/p3/serviceValidate")))
            .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        CasIdentityClaims claims = validator.validate("ST-array");

        assertThat(claims.subject()).isEqualTo("wangwu");
        assertThat(claims.providerLogin()).isEqualTo("Wang Wu");
        assertThat(claims.email()).isEqualTo("wangwu@example.com");

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
}
