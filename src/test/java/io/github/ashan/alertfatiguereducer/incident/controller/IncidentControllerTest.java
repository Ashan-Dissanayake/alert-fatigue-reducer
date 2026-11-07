package io.github.ashan.alertfatiguereducer.incident.controller;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.dto.response.IncidentResponse;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import io.github.ashan.alertfatiguereducer.shared.config.PaginationConfig;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
@Import(PaginationConfig.class)
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

        verify(incidentService).getIncidentById(10L);
    }

    @Test
    void shouldReturnNotFoundWhenIncidentDoesNotExist() throws Exception {
        when(incidentService.getIncidentById(99L))
                .thenThrow(new IncidentNotFoundException(99L));

        mockMvc.perform(get("/api/v1/incidents/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Incident Not Found"))
                .andExpect(jsonPath("$.message").value("Incident not found: 99"));

        verify(incidentService).getIncidentById(99L);
    }

    @Test
    void shouldReturnPagedIncidentsWithDefaultPagination() throws Exception {
        IncidentResponse response = new IncidentResponse(
                1L,
                "Service Down",
                "payment-service",
                AlertEnvironment.PRODUCTION,
                IncidentSeverity.CRITICAL,
                IncidentStatus.OPEN,
                LocalDateTime.of(2026, 10, 10, 8, 0),
                LocalDateTime.of(2026, 10, 10, 8, 10),
                0.90
        );

        Page<IncidentResponse> page =
                new PageImpl<>(List.of(response));

        when(incidentService.getAllIncidents(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Service Down"))
                .andExpect(jsonPath("$.content[0].service").value("payment-service"))
                .andExpect(jsonPath("$.page.totalElements").value(1));

        verify(incidentService).getAllIncidents(
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void shouldFilterIncidentsByStatusAndEnvironment() throws Exception {
        when(incidentService.getAllIncidents(
                eq(IncidentStatus.OPEN),
                eq(AlertEnvironment.PRODUCTION),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/incidents")
                        .param("status", "OPEN")
                        .param("environment", "PRODUCTION"))
                .andExpect(status().isOk());

        verify(incidentService).getAllIncidents(
                eq(IncidentStatus.OPEN),
                eq(AlertEnvironment.PRODUCTION),
                any(Pageable.class)
        );
    }

    @Test
    void shouldPassPageableParametersCorrectly() throws Exception {
        when(incidentService.getAllIncidents(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/incidents")
                        .param("page", "2")
                        .param("size", "10"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(incidentService).getAllIncidents(
                isNull(),
                isNull(),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(2, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void shouldReturnEmptyPageWhenNoIncidentsMatch() throws Exception {
        when(incidentService.getAllIncidents(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.totalElements").value(0));

        verify(incidentService).getAllIncidents(
                isNull(),
                isNull(),
                any(Pageable.class)
        );
    }

    @Test
    void shouldUseDefaultSortingWhenNoSortIsProvided() throws Exception {
        when(incidentService.getAllIncidents(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/incidents"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(incidentService).getAllIncidents(
                isNull(),
                isNull(),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals("lastUpdatedAt", pageable.getSort().iterator().next().getProperty());
        assertEquals(
                Sort.Direction.DESC,
                pageable.getSort().iterator().next().getDirection()
        );
    }

    @Test
    void shouldUseCustomSortingWhenSortParameterIsProvided() throws Exception {
        when(incidentService.getAllIncidents(
                isNull(),
                isNull(),
                any(Pageable.class)
        )).thenReturn(Page.empty());

        mockMvc.perform(get("/api/v1/incidents")
                        .param("sort", "severity,asc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(incidentService).getAllIncidents(
                isNull(),
                isNull(),
                pageableCaptor.capture()
        );

        Sort.Order order = pageableCaptor.getValue()
                .getSort()
                .getOrderFor("severity");

        assertNotNull(order);
        assertEquals(Sort.Direction.ASC, order.getDirection());
    }

    @Test
    void shouldRejectUnsupportedSortField() throws Exception {
        mockMvc.perform(get("/api/v1/incidents")
                        .param("sort", "unknownField,asc"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(incidentService);
    }

    @Test
    void shouldResolveIncidentSuccessfully() throws Exception {
        doNothing().when(incidentService).resolveIncident(10L);

        mockMvc.perform(put("/api/v1/incidents/10/resolve"))
                .andExpect(status().isNoContent());

        verify(incidentService).resolveIncident(10L);
    }

    @Test
    void shouldReturnNotFoundWhenResolvingNonExistentIncident() throws Exception {
        doThrow(new IncidentNotFoundException(99L))
                .when(incidentService).resolveIncident(99L);

        mockMvc.perform(put("/api/v1/incidents/99/resolve"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Incident Not Found"))
                .andExpect(jsonPath("$.message").value("Incident not found: 99"));

        verify(incidentService).resolveIncident(99L);
    }
}
