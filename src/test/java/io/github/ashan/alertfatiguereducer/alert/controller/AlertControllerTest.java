package io.github.ashan.alertfatiguereducer.alert.controller;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSeverity;
import io.github.ashan.alertfatiguereducer.alert.entity.AlertSource;
import io.github.ashan.alertfatiguereducer.alert.service.AlertService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

        mockMvc.perform(
                        post("/api/v1/alerts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated());
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
}