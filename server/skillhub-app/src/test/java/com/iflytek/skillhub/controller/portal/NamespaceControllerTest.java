package com.iflytek.skillhub.controller.portal;

import com.iflytek.skillhub.TestRedisConfig;
import com.iflytek.skillhub.auth.device.DeviceAuthService;
import com.iflytek.skillhub.domain.shared.exception.DomainBadRequestException;
import com.iflytek.skillhub.domain.shared.exception.DomainForbiddenException;
import com.iflytek.skillhub.dto.MessageResponse;
import com.iflytek.skillhub.service.GovernanceWorkflowAppService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestRedisConfig.class)
class NamespaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GovernanceWorkflowAppService governanceWorkflowAppService;

    @MockBean
    private DeviceAuthService deviceAuthService;

    @Test
    void deleteNamespace_success_whenArchivedAndOwner() throws Exception {
        given(governanceWorkflowAppService.deleteNamespace(
                eq("test-ns"),
                any(),
                eq("usr_1"),
                any()))
                .willReturn(new MessageResponse("Namespace deleted successfully"));

        mockMvc.perform(delete("/api/web/namespaces/test-ns")
                        .requestAttr("userId", "usr_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"No longer needed\"}")
                        .with(user("usr_1"))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.message").exists());
    }

    @Test
    void deleteNamespace_fails_whenNotArchived() throws Exception {
        doThrow(new DomainBadRequestException("error.namespace.delete.mustBeArchived", "test-ns"))
                .when(governanceWorkflowAppService).deleteNamespace(
                        eq("test-ns"),
                        any(),
                        eq("usr_1"),
                        any());

        mockMvc.perform(delete("/api/web/namespaces/test-ns")
                        .requestAttr("userId", "usr_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"No longer needed\"}")
                        .with(user("usr_1"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void deleteNamespace_fails_whenNotOwner() throws Exception {
        doThrow(new DomainForbiddenException("error.namespace.lifecycle.forbidden", "test-ns"))
                .when(governanceWorkflowAppService).deleteNamespace(
                        eq("test-ns"),
                        any(),
                        eq("usr_2"),
                        any());

        mockMvc.perform(delete("/api/web/namespaces/test-ns")
                        .requestAttr("userId", "usr_2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"No longer needed\"}")
                        .with(user("usr_2"))
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403));
    }

    @Test
    void deleteNamespace_fails_whenHasActiveSkills() throws Exception {
        doThrow(new DomainBadRequestException("error.namespace.delete.hasActiveSkills", "test-ns"))
                .when(governanceWorkflowAppService).deleteNamespace(
                        eq("test-ns"),
                        any(),
                        eq("usr_1"),
                        any());

        mockMvc.perform(delete("/api/web/namespaces/test-ns")
                        .requestAttr("userId", "usr_1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\":\"No longer needed\"}")
                        .with(user("usr_1"))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
