package io.github.ashan.alertfatiguereducer.alert.controller;

import io.github.ashan.alertfatiguereducer.alert.dto.request.CreateAlertRequest;
import io.github.ashan.alertfatiguereducer.alert.dto.response.AlertResponse;
import io.github.ashan.alertfatiguereducer.alert.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService alertService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlertResponse createAlert(
            @Valid @RequestBody CreateAlertRequest request
    ) {
        return alertService.createAlert(request);
    }
}