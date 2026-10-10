package io.github.ashan.alertfatiguereducer.incident.controller;

import io.github.ashan.alertfatiguereducer.incident.entity.Incident;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping("/{incidentId}")
    public ResponseEntity<Incident> getIncidentById(
            @PathVariable Long incidentId) {

        Incident incident = incidentService.getIncidentById(incidentId);

        return ResponseEntity.ok(incident);
    }
}