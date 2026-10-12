
package io.github.ashan.alertfatiguereducer.shared.exception;

import io.github.ashan.alertfatiguereducer.shared.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IncidentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleIncidentNotFound(
            IncidentNotFoundException exception) {

        ErrorResponse body = new ErrorResponse(
                "Incident Not Found",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(IncidentNotOpenException.class)
    public ResponseEntity<ErrorResponse> handleIncidentNotOpen(
            IncidentNotOpenException exception) {

        ErrorResponse body = new ErrorResponse(
                "Incident Not Open",
                exception.getMessage()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "
                        + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        ErrorResponse body = new ErrorResponse(
                "Validation Failed",
                message
        );

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception) {

        ErrorResponse body = new ErrorResponse(
                "Invalid Request",
                "Request body is malformed or contains invalid field values"
        );

        return ResponseEntity.badRequest().body(body);
    }
}
