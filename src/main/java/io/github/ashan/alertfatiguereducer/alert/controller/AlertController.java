package io.github.ashan.alertfatiguereducer.alert.controller;


import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertCorrelationResponse;
import io.github.ashan.alertfatiguereducer.alert.service.AlertService;
import io.github.ashan.alertfatiguereducer.shared.response.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @Operation(
            summary = "Submit an alert",
            description = """
                    Accepts a monitoring alert and evaluates it using
                    the configured correlation rules.

                    The alert is either attached to a matching open incident
                    or used to create a new incident. The response includes
                    the correlation action, incident ID, and correlation score.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Alert accepted and correlation evaluated"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request validation failed or request body is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertCorrelationResponse createAlert(
            @Valid @RequestBody CreateAlertRequest request
    ) {
        return alertService.createAlert(request);
    }
}
