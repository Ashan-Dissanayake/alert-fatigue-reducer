
package io.github.ashan.alertfatiguereducer.incident.controller;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService incidentService;

    @Test
    void shouldReturnIncidentWhenIncidentExists() throws Exception {
        Incident incident = new Incident();
        incident.setId(10L);
        incident.setTitle("High CPU usage");
        incident.setService("order-service");
        incident.setEnvironment(AlertEnvironment.PRODUCTION);
        incident.setSeverity(IncidentSeverity.WARNING);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setStartedAt(LocalDateTime.of(2026, 10, 10, 8, 0));
        incident.setLastUpdatedAt(LocalDateTime.of(2026, 10, 10, 8, 5));
        incident.setCorrelationScore(0.75);

        when(incidentService.getIncidentById(10L)).thenReturn(incident);

        mockMvc.perform(get("/api/v1/incidents/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("High CPU usage"))
                .andExpect(jsonPath("$.service").value("order-service"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.correlationScore").value(0.75));
    }

    @Test
    void shouldReturnNotFoundWhenIncidentDoesNotExist() throws Exception {
        when(incidentService.getIncidentById(999L))
                .thenThrow(new IncidentNotFoundException(999L));

        mockMvc.perform(get("/api/v1/incidents/999"))
                .andExpect(status().isNotFound());
    }
}
