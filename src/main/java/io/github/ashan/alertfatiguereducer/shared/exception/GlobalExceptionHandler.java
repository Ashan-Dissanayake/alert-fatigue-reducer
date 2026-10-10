package io.github.ashan.alertfatiguereducer.shared.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleIncidentNotFound(
            IncidentNotFoundException exception) {

        Map<String, String> body = Map.of(
                "error", "Incident Not Found",
                "message", exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}
