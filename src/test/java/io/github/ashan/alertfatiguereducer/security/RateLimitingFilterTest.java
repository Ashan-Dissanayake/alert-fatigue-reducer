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
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AlertController.class, IncidentController.class})
@Import({SecurityConfig.class, RateLimitingFilter.class, RateLimitingProperties.class, PaginationConfig.class})
class RateLimitingFilterTest {

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

    @BeforeEach
    void setUp() {
        // Configure strict low limit for testing burst exhaustion: capacity=2, refill=1 token/sec
        rateLimitingProperties.setEnabled(true);
        rateLimitingProperties.setCapacity(2);
        rateLimitingProperties.setRefillTokensPerSecond(1);

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "alert-producer", roles = {"INGESTION"})
    void shouldReturn429WhenBurstCapacityIsExceeded() throws Exception {
        CreateAlertRequest request = new CreateAlertRequest(
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

        when(alertService.createAlert(any(CreateAlertRequest.class)))
                .thenReturn(new AlertCorrelationResponse(
                        null,
                        CorrelationAction.NEW_INCIDENT,
                        1L,
                        0.0,
                        List.of(),
                        "v1.0"
                ));

        String payload = objectMapper.writeValueAsString(request);

        // 1st request -> Accepted (201)
        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        // 2nd request -> Accepted (201)
        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        // 3rd request -> Rate limited (429 Too Many Requests)
        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "1"))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("Rate limit exceeded")));
    }
}
