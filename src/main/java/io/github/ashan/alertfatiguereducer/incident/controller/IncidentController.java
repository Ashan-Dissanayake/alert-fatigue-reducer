package io.github.ashan.alertfatiguereducer.incident.controller;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.dto.response.IncidentResponse;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import io.github.ashan.alertfatiguereducer.shared.config.AllowedIncidentSortFields;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentResponse> getIncidentById(
            @PathVariable Long incidentId) {

        IncidentResponse incidentResponse =
                incidentService.getIncidentById(incidentId);

        return ResponseEntity.ok(incidentResponse);
    }

    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> getAllIncidents(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) AlertEnvironment environment,
            @PageableDefault(
                    size = 20,
                    sort = "lastUpdatedAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        boolean hasInvalidSortField = pageable.getSort()
                .stream()
                .anyMatch(order ->
                        !AllowedIncidentSortFields.FIELDS.contains(order.getProperty())
                );

        if (hasInvalidSortField) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported sort field. Allowed fields: "
                            + AllowedIncidentSortFields.FIELDS
            );
        }

        Page<IncidentResponse> incidents =
                incidentService.getAllIncidents(status, environment, pageable);

        return ResponseEntity.ok(incidents);
    }
}