
package io.github.ashan.alertfatiguereducer.incident.controller;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.dto.response.IncidentResponse;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
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
        IncidentResponse response = new IncidentResponse(
                10L,
                "High CPU usage",
                "order-service",
                AlertEnvironment.PRODUCTION,
                IncidentSeverity.WARNING,
                IncidentStatus.OPEN,
                LocalDateTime.of(2026, 10, 10, 8, 0),
                LocalDateTime.of(2026, 10, 10, 8, 5),
                0.75
        );

        when(incidentService.getIncidentById(10L))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/incidents/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("High CPU usage"))
                .andExpect(jsonPath("$.service").value("order-service"))
                .andExpect(jsonPath("$.environment").value("PRODUCTION"))
                .andExpect(jsonPath("$.severity").value("WARNING"))
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

    @Test
    void shouldReturnAllIncidents() throws Exception {
        IncidentResponse incident = createIncidentResponse();

        Page<IncidentResponse> page = new PageImpl<>(
                List.of(incident),
                org.springframework.data.domain.PageRequest.of(0, 20),
                1
        );

        when(incidentService.getAllIncidents(
                isNull(), isNull(), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(10))
                .andExpect(jsonPath("$.content[0].service").value("order-service"))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    void shouldFilterIncidentsByStatus() throws Exception {
        IncidentResponse incident = createIncidentResponse();

        Page<IncidentResponse> page = new PageImpl<>(
                List.of(incident),
                org.springframework.data.domain.PageRequest.of(0, 20),
                1
        );

        when(incidentService.getAllIncidents(
                eq(IncidentStatus.OPEN), isNull(), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/incidents")
                        .param("status", "OPEN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].status").value("OPEN"));
    }

    @Test
    void shouldFilterIncidentsByEnvironment() throws Exception {
        IncidentResponse incident = createIncidentResponse();

        Page<IncidentResponse> page = new PageImpl<>(
                List.of(incident),
                org.springframework.data.domain.PageRequest.of(0, 20),
                1
        );

        when(incidentService.getAllIncidents(
                isNull(), eq(AlertEnvironment.PRODUCTION), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/incidents")
                        .param("environment", "PRODUCTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].environment").value("PRODUCTION"));
    }

    private IncidentResponse createIncidentResponse() {
        return new IncidentResponse(
                10L,
                "High CPU usage",
                "order-service",
                AlertEnvironment.PRODUCTION,
                IncidentSeverity.WARNING,
                IncidentStatus.OPEN,
                LocalDateTime.of(2026, 10, 10, 8, 0),
                LocalDateTime.of(2026, 10, 10, 8, 5),
                0.75
        );
    }
}