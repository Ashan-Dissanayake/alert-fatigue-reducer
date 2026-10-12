
package io.github.ashan.alertfatiguereducer.incident.controller;

import io.github.ashan.alertfatiguereducer.alert.entity.AlertEnvironment;
import io.github.ashan.alertfatiguereducer.incident.dto.response.IncidentResponse;
import io.github.ashan.alertfatiguereducer.incident.entity.IncidentStatus;
import io.github.ashan.alertfatiguereducer.incident.service.IncidentService;
import io.github.ashan.alertfatiguereducer.shared.config.AllowedIncidentSortFields;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

    @Operation(
            summary = "Get an incident by ID",
            description = """
                    Retrieves the details of a specific incident,
                    including its service, environment, severity,
                    status, timestamps, and correlation score.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incident retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Incident not found"
            )
    })
    @GetMapping("/{incidentId}")
    public ResponseEntity<IncidentResponse> getIncidentById(
            @Parameter(
                    description = "Unique identifier of the incident",
                    example = "1",
                    required = true
            )
            @PathVariable Long incidentId) {

        IncidentResponse incidentResponse =
                incidentService.getIncidentById(incidentId);

        return ResponseEntity.ok(incidentResponse);
    }

    @Operation(
            summary = "List incidents",
            description = """
                    Retrieves a paginated list of incidents.
                    Results can be filtered by incident status and environment.

                    Defaults to page 0, size 20, sorted by lastUpdatedAt
                    in descending order. Supported sort fields are defined
                    by the API's allowed incident sort fields.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Incidents retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid filter, pagination, or unsupported sort field"
            )
    })
    @GetMapping
    public ResponseEntity<Page<IncidentResponse>> getAllIncidents(
            @Parameter(
                    description = "Filter by incident lifecycle status",
                    example = "OPEN"
            )
            @RequestParam(required = false) IncidentStatus status,

            @Parameter(
                    description = "Filter by deployment environment",
                    example = "PRODUCTION"
            )
            @RequestParam(required = false) AlertEnvironment environment,

            @ParameterObject
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
