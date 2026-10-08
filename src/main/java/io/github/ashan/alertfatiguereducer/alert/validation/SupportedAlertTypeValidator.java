package io.github.ashan.alertfatiguereducer.alert.validation;


import io.github.ashan.alertfatiguereducer.alert.entity.AlertType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

public class SupportedAlertTypeValidator
        implements ConstraintValidator<SupportedAlertType, String> {

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) {
            return true;
        }

        return Arrays.stream(AlertType.values())
                .anyMatch(type -> type.name().equals(value));
    }
}