package io.github.ashan.alertfatiguereducer.alert.controller;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertCorrelationResponse;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertResponse;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.service.AlertService;
import io.github.ashan.alertfatiguereducer.incident.correlation.CorrelationAction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlertService alertService;


    @Test
    void shouldAcceptValidAlertRequest() throws Exception {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "CPU usage exceeded 90%",
                LocalDateTime.of(2026, 10, 8, 9, 31),
                "cpu_usage",
                94.5,
                90.0
        );

        AlertResponse alertResponse = new AlertResponse(
                1L,
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "CPU usage exceeded 90%",
                LocalDateTime.of(2026, 10, 8, 9, 31),
                "cpu_usage",
                94.5,
                90.0,
                LocalDateTime.of(2026, 10, 8, 9, 32)
        );

        AlertCorrelationResponse correlationResponse =
                new AlertCorrelationResponse(
                        alertResponse,
                        CorrelationAction.NEW_INCIDENT,
                        10L,
                        0.0
                );

        when(alertService.createAlert(any(CreateAlertRequest.class)))
                .thenReturn(correlationResponse);

        mockMvc.perform(
                        post("/api/v1/alerts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alert.id").value(1))
                .andExpect(jsonPath("$.alert.service").value("order-service"))
                .andExpect(jsonPath("$.alert.type").value("CPU_HIGH"))
                .andExpect(jsonPath("$.correlationAction").value("NEW_INCIDENT"))
                .andExpect(jsonPath("$.incidentId").value(10))
                .andExpect(jsonPath("$.correlationScore").value(0.0));

        verify(alertService).createAlert(any(CreateAlertRequest.class));
    }

    @Test
    void shouldRejectRequestWhenServiceIsBlank() throws Exception {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "CPU usage exceeded 90%",
                LocalDateTime.of(2026, 10, 8, 9, 31),
                "cpu_usage",
                94.5,
                90.0
        );

        mockMvc.perform(
                        post("/api/v1/alerts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alertService);
    }

    @Test
    void shouldRejectRequestWhenServiceIsMissing() throws Exception {

        String request = """
                {
                  "source": "CUSTOM",
                  "environment": "PRODUCTION",
                  "type": "CPU_HIGH",
                  "severity": "WARNING",
                  "timestamp": "2026-10-08T09:31:00"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/alerts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alertService);
    }

    @Test
    void shouldRejectRequestWhenTimestampIsMissing() throws Exception {

        String request = """
                {
                  "source": "CUSTOM",
                  "service": "order-service",
                  "environment": "PRODUCTION",
                  "type": "CPU_HIGH",
                  "severity": "WARNING"
                }
                """;

        mockMvc.perform(
                        post("/api/v1/alerts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alertService);
    }

    @Test
    void shouldRejectFutureTimestamp() throws Exception {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "CPU_HIGH",
                AlertSeverity.WARNING,
                "CPU usage exceeded 90%",
                LocalDateTime.now().plusMinutes(5),
                "cpu_usage",
                94.5,
                90.0
        );

        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alertService);
    }

    @Test
    void shouldRejectUnsupportedAlertType() throws Exception {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "UNKNOWN_ALERT",
                AlertSeverity.WARNING,
                "Unknown alert type",
                LocalDateTime.now().minusMinutes(1),
                "unknown_metric",
                100.0,
                90.0
        );

        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alertService);
    }

    @Test
    void shouldRejectLowercaseAlertType() throws Exception {

        CreateAlertRequest request = new CreateAlertRequest(
                AlertSource.CUSTOM,
                "order-service",
                AlertEnvironment.PRODUCTION,
                "cpu_high",
                AlertSeverity.WARNING,
                "CPU usage exceeded threshold",
                LocalDateTime.now().minusMinutes(1),
                "cpu_usage",
                94.5,
                90.0
        );

        mockMvc.perform(post("/api/v1/alerts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(alertService);
    }
}