package io.github.ashan.alertfatiguereducer.shared.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "ErrorResponse",
        description = "Standard error response returned by the API"
)
public record ErrorResponse(

        @Schema(
                description = "Short description of the error",
                example = "Validation Failed"
        )
        String error,

        @Schema(
                description = "Details explaining why the request failed",
                example = "service: must not be blank"
        )
        String message
) {
}
