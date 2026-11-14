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
import io.github.ashan.alertfatiguereducer.shared.config.RateLimitingFilter;
import io.github.ashan.alertfatiguereducer.shared.config.RateLimitingProperties;
import io.github.ashan.alertfatiguereducer.shared.config.SecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AlertController.class, IncidentController.class})
@Import({SecurityConfig.class, RateLimitingFilter.class, RateLimitingProperties.class, PaginationConfig.class})
class SecurityPenetrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RateLimitingProperties rateLimitingProperties;

    @MockitoBean
    private AlertService alertService;

    @MockitoBean
    private IncidentService incidentService;

    private static final String ADMIN_AUTH = basicAuth("admin", "password123");
    private static final String INGESTION_AUTH = basicAuth("ingestion", "ingestion123");

    private static String basicAuth(String username, String password) {
        String token = username + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setUp() {
        // Disable rate limiting during penetration payload tests to isolate injection/tampering tests
        rateLimitingProperties.setEnabled(false);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    // ── 1. Authentication Tampering & Credential Safety ──────────────────────

    @Test
    void shouldRejectInvalidCredentialsWith401WithoutLeakingAccountStatus() throws Exception {
        String invalidAuth = basicAuth("admin", "wrong-password");

        mockMvc.perform(get("/api/v1/incidents")
                        .header(HttpHeaders.AUTHORIZATION, invalidAuth))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectNonExistentUserWithConsistent401() throws Exception {
        String nonExistentAuth = basicAuth("ghost_user", "some_password");

        mockMvc.perform(get("/api/v1/incidents")
                        .header(HttpHeaders.AUTHORIZATION, nonExistentAuth))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRejectMalformedAuthorizationHeader() throws Exception {
        mockMvc.perform(get("/api/v1/incidents")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-format"))
                .andExpect(status().isUnauthorized());
    }

    // ── 2. HTTP Verb Tampering ───────────────────────────────────────────────

    @Test
    void shouldRejectUnsupportedHttpMethodOnAlertsEndpoint() throws Exception {
        // DELETE is not allowed on /api/v1/alerts
        mockMvc.perform(delete("/api/v1/alerts")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_AUTH))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void shouldRejectUnsupportedHttpMethodOnIncidentResolveEndpoint() throws Exception {
        // POST is not allowed on /api/v1/incidents/{id}/resolve (only PUT)
        mockMvc.perform(post("/api/v1/incidents/1/resolve")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_AUTH))
                .andExpect(status().isMethodNotAllowed());
    }

    // ── 3. Path Parameter Tampering & Type Safety ────────────────────────────

    @Test
    void shouldRejectNonNumericIncidentIdWithPathTraversalAttempt() throws Exception {
        mockMvc.perform(get("/api/v1/incidents/..%2F..%2Fetc%2Fpasswd")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_AUTH))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectAlphabeticalIncidentId() throws Exception {
        mockMvc.perform(get("/api/v1/incidents/not-a-number")
                        .header(HttpHeaders.AUTHORIZATION, ADMIN_AUTH))
                .andExpect(status().isBadRequest());
    }

    // ── 4. Injection Resilience in Payload Fields ────────────────────────────

    @Test
    void shouldSafelyHandleHtmlAndScriptTagsInAlertMessage() throws Exception {
        // Alert payloads containing HTML/XSS markup must be processed safely as plain text
        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "security-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "<script>alert('xss-payload')</script>",
                LocalDateTime.now().minusMinutes(1),
                "cpu",
                95.0,
                80.0
        );

        when(alertService.createAlert(any(CreateAlertRequest.class)))
                .thenReturn(new AlertCorrelationResponse(
                        null,
                        CorrelationAction.NEW_INCIDENT,
                        10L,
                        0.0,
                        List.of(),
                        "v1.0"
                ));

        mockMvc.perform(post("/api/v1/alerts")
                        .header(HttpHeaders.AUTHORIZATION, INGESTION_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.correlationAction").value("NEW_INCIDENT"));
    }

    @Test
    void shouldSafelyHandleSqlCharactersInServiceAndMessage() throws Exception {
        // SQL injection probe in text field
        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service'; DROP TABLE alerts; --",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.CRITICAL,
                "' OR '1'='1",
                LocalDateTime.now().minusMinutes(1),
                "cpu",
                99.0,
                80.0
        );

        when(alertService.createAlert(any(CreateAlertRequest.class)))
                .thenReturn(new AlertCorrelationResponse(
                        null,
                        CorrelationAction.NEW_INCIDENT,
                        11L,
                        0.0,
                        List.of(),
                        "v1.0"
                ));

        mockMvc.perform(post("/api/v1/alerts")
                        .header(HttpHeaders.AUTHORIZATION, INGESTION_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    // ── 5. Numeric Bound & Floating Point Tampering ──────────────────────────

    @Test
    void shouldRejectNaNAndInfiniteValuesInAlertPayload() throws Exception {
        String payload = """
                {
                  "source": "CUSTOM",
                  "service": "order-service",
                  "environment": "PRODUCTION",
                  "type": "CPU_HIGH",
                  "severity": "WARNING",
                  "message": "Probe",
                  "timestamp": "2026-10-08T09:31:00",
                  "metric": "cpu",
                  "value": Infinity,
                  "threshold": 90.0
                }
                """;

        mockMvc.perform(post("/api/v1/alerts")
                        .header(HttpHeaders.AUTHORIZATION, INGESTION_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }
}
