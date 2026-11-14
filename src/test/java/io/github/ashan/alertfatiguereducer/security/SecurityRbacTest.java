package io.github.ashan.alertfatiguereducer.security;

import io.github.ashan.alertfatiguereducer.alert.controller.AlertController;
import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertCorrelationResponse;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.service.AlertService;
import io.github.ashan.alertfatiguereducer.incident.controller.IncidentController;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationAction;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import io.github.ashan.alertfatiguereducer.shared.config.PaginationConfig;
import io.github.ashan.alertfatiguereducer.shared.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AlertController.class, IncidentController.class})
@Import({SecurityConfig.class, PaginationConfig.class})
class SecurityRbacTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertService alertService;

    @MockitoBean
    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ── Unauthenticated Access ───────────────────────────────────────────────

    @Test
    void shouldRejectUnauthenticatedAlertSubmission() throws Exception {
        CreateAlertRequest request = createSampleRequest();

        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedIncidentList() throws Exception {
        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectUnauthenticatedIncidentResolve() throws Exception {
        mockMvc.perform(put("/api/v1/incidents/1/resolve"))
                .andExpect(status().isUnauthorized());
    }

    // ── INGESTION Role Permissions ───────────────────────────────────────────

    @Test
    @WithMockUser(username = "ingestion-bot", roles = {"INGESTION"})
    void shouldAllowIngestionRoleToSubmitAlerts() throws Exception {
        CreateAlertRequest request = createSampleRequest();

        when(alertService.createAlert(any(CreateAlertRequest.class)))
                .thenReturn(new AlertCorrelationResponse(
                        null,
                        CorrelationAction.NEW_INCIDENT,
                        1L,
                        0.0,
                        List.of(),
                        "v1.0"
                ));

        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "ingestion-bot", roles = {"INGESTION"})
    void shouldForbidIngestionRoleFromReadingIncidents() throws Exception {
        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "ingestion-bot", roles = {"INGESTION"})
    void shouldForbidIngestionRoleFromResolvingIncidents() throws Exception {
        mockMvc.perform(put("/api/v1/incidents/1/resolve"))
                .andExpect(status().isForbidden());
    }

    // ── OPERATOR Role Permissions ────────────────────────────────────────────

    @Test
    @WithMockUser(username = "operator-user", roles = {"OPERATOR"})
    void shouldAllowOperatorRoleToListIncidents() throws Exception {
        when(incidentService.getAllIncidents(any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "operator-user", roles = {"OPERATOR"})
    void shouldForbidOperatorRoleFromResolvingIncidents() throws Exception {
        mockMvc.perform(put("/api/v1/incidents/1/resolve"))
                .andExpect(status().isForbidden());
    }

    // ── ADMIN Role Permissions ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    void shouldAllowAdminRoleToResolveIncidents() throws Exception {
        doNothing().when(incidentService).resolveIncident(1L);

        mockMvc.perform(put("/api/v1/incidents/1/resolve"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin-user", roles = {"ADMIN"})
    void shouldAllowAdminRoleToListIncidents() throws Exception {
        when(incidentService.getAllIncidents(any(), any(), any()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk());
    }

    private CreateAlertRequest createSampleRequest() {
        return new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "High CPU",
                LocalDateTime.now().minusMinutes(1),
                "cpu",
                95.0,
                80.0
        );
    }
}
