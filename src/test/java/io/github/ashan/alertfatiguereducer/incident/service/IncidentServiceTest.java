package io.github.ashan.alertfatiguereducer.incident.service;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.dto.response.IncidentResponse;
import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentSeverity;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.mapper.IncidentMapper;
import io.github.ashan.alertfatiguereducer.incident.repository.IncidentRepository;
import io.github.ashan.alertfatiguereducer.shared.exception.IncidentNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository incidentRepository;

    @Mock
    private IncidentMapper incidentMapper;

    private IncidentService incidentService;

    @BeforeEach
    void setUp() {
        incidentService = new IncidentService(
                incidentRepository,
                incidentMapper
        );
    }

    @Test
    void shouldCreateOpenIncident() {
        LocalDateTime occurredAt = LocalDateTime.of(2026, 10, 9, 10, 0);

        CreateIncidentCommand command = new CreateIncidentCommand(
                "Order Service Degradation",
                "order-service",
                AlertEnvironment.PRODUCTION,
                IncidentSeverity.CRITICAL,
                occurredAt,
                0.85
        );

        when(incidentRepository.save(any(Incident.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Incident result = incidentService.createIncident(command);

        assertEquals("Order Service Degradation", result.getTitle());
        assertEquals("order-service", result.getService());
        assertEquals(AlertEnvironment.PRODUCTION, result.getEnvironment());
        assertEquals(IncidentSeverity.CRITICAL, result.getSeverity());
        assertEquals(IncidentStatus.OPEN, result.getStatus());
        assertEquals(occurredAt, result.getStartedAt());
        assertEquals(occurredAt, result.getLastUpdatedAt());
        assertEquals(0.85, result.getCorrelationScore());

        verify(incidentRepository).save(any(Incident.class));
    }

    @Test
    void shouldResolveOpenIncident() {
        Incident incident = new Incident();
        incident.setId(1L);
        incident.setStatus(IncidentStatus.OPEN);
        incident.setLastUpdatedAt(LocalDateTime.now().minusMinutes(10));

        when(incidentRepository.findById(1L))
                .thenReturn(Optional.of(incident));

        incidentService.resolveIncident(1L);

        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertTrue(incident.getLastUpdatedAt()
                .isAfter(LocalDateTime.now().minusMinutes(1)));

        verify(incidentRepository).findById(1L);
        verify(incidentRepository, never()).save(any());
    }

    @Test
    void shouldNotChangeAlreadyResolvedIncident() {
        Incident incident = new Incident();
        incident.setId(1L);
        incident.setStatus(IncidentStatus.RESOLVED);

        LocalDateTime previousUpdatedAt =
                LocalDateTime.of(2026, 10, 8, 12, 0);

        incident.setLastUpdatedAt(previousUpdatedAt);

        when(incidentRepository.findById(1L))
                .thenReturn(Optional.of(incident));

        incidentService.resolveIncident(1L);

        assertEquals(IncidentStatus.RESOLVED, incident.getStatus());
        assertEquals(previousUpdatedAt, incident.getLastUpdatedAt());
    }

    @Test
    void shouldThrowWhenIncidentDoesNotExist() {
        when(incidentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                IncidentNotFoundException.class,
                () -> incidentService.resolveIncident(99L)
        );
    }

    @Test
    void shouldReturnAllIncidentsWhenNoFiltersProvided() {
        Incident incident = new Incident();
        incident.setId(1L);
        incident.setTitle("High CPU usage");
        incident.setService("order-service");
        incident.setEnvironment(AlertEnvironment.PRODUCTION);
        incident.setSeverity(IncidentSeverity.WARNING);
        incident.setStatus(IncidentStatus.OPEN);

        mockFilteredIncidents(List.of(incident));

        IncidentResponse response = new IncidentResponse(
                1L,
                "High CPU usage",
                "order-service",
                AlertEnvironment.PRODUCTION,
                IncidentSeverity.WARNING,
                IncidentStatus.OPEN,
                null,
                null,
                null
        );

        when(incidentMapper.toResponse(incident)).thenReturn(response);

        List<IncidentResponse> result =
                incidentService.getAllIncidents(null, null);

        assertEquals(1, result.size());
        assertEquals("order-service", result.get(0).service());

        verify(incidentRepository).findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<Incident>>any()
        );
    }

    @Test
    void shouldFilterIncidentsByStatus() {
        Incident incident = new Incident();
        incident.setId(1L);
        incident.setTitle("High CPU usage");
        incident.setService("order-service");
        incident.setEnvironment(AlertEnvironment.PRODUCTION);
        incident.setSeverity(IncidentSeverity.WARNING);
        incident.setStatus(IncidentStatus.OPEN);

        mockFilteredIncidents(List.of(incident));

        IncidentResponse response = new IncidentResponse(
                1L,
                "High CPU usage",
                "order-service",
                AlertEnvironment.PRODUCTION,
                IncidentSeverity.WARNING,
                IncidentStatus.OPEN,
                null,
                null,
                null
        );

        when(incidentMapper.toResponse(incident)).thenReturn(response);

        List<IncidentResponse> result =
                incidentService.getAllIncidents(IncidentStatus.OPEN, null);

        assertEquals(1, result.size());
        assertEquals(IncidentStatus.OPEN, result.get(0).status());

        verify(incidentRepository).findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<Incident>>argThat(spec -> spec != null)
        );
    }

    @Test
    void shouldFilterIncidentsByEnvironment() {
        Incident incident = new Incident();
        incident.setId(1L);
        incident.setTitle("High CPU usage");
        incident.setService("order-service");
        incident.setEnvironment(AlertEnvironment.PRODUCTION);
        incident.setSeverity(IncidentSeverity.WARNING);
        incident.setStatus(IncidentStatus.OPEN);

        mockFilteredIncidents(List.of(incident));

        IncidentResponse response = new IncidentResponse(
                1L,
                "High CPU usage",
                "order-service",
                AlertEnvironment.PRODUCTION,
                IncidentSeverity.WARNING,
                IncidentStatus.OPEN,
                null,
                null,
                null
        );

        when(incidentMapper.toResponse(incident)).thenReturn(response);

        List<IncidentResponse> result =
                incidentService.getAllIncidents(
                        null, AlertEnvironment.PRODUCTION
                );

        assertEquals(1, result.size());
        assertEquals(
                AlertEnvironment.PRODUCTION,
                result.get(0).environment()
        );

        verify(incidentRepository).findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<Incident>>argThat(spec -> spec != null)
        );
    }

    @Test
    void shouldFilterIncidentsByStatusAndEnvironment() {
        Incident incident = new Incident();
        incident.setId(1L);
        incident.setTitle("High CPU usage");
        incident.setService("order-service");
        incident.setEnvironment(AlertEnvironment.PRODUCTION);
        incident.setSeverity(IncidentSeverity.WARNING);
        incident.setStatus(IncidentStatus.OPEN);

        mockFilteredIncidents(List.of(incident));

        IncidentResponse response = new IncidentResponse(
                1L,
                "High CPU usage",
                "order-service",
                AlertEnvironment.PRODUCTION,
                IncidentSeverity.WARNING,
                IncidentStatus.OPEN,
                null,
                null,
                null
        );

        when(incidentMapper.toResponse(incident)).thenReturn(response);

        List<IncidentResponse> result =
                incidentService.getAllIncidents(
                        IncidentStatus.OPEN,
                        AlertEnvironment.PRODUCTION
                );

        assertEquals(1, result.size());
        assertEquals(IncidentStatus.OPEN, result.get(0).status());
        assertEquals(
                AlertEnvironment.PRODUCTION,
                result.get(0).environment()
        );

        verify(incidentRepository).findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<Incident>>argThat(spec -> spec != null)
        );
    }

    private void mockFilteredIncidents(List<Incident> incidents) {
        when(incidentRepository.findAll(
                org.mockito.ArgumentMatchers
                        .<Specification<Incident>>any()
        )).thenReturn(incidents);
    }
}