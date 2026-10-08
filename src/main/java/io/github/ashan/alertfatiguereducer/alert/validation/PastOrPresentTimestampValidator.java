package io.github.ashan.alertfatiguereducer.alert.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class PastOrPresentTimestampValidator
        implements ConstraintValidator<PastOrPresentTimestamp, LocalDateTime> {

    @Override
    public boolean isValid(
            LocalDateTime value,
            ConstraintValidatorContext context) {

        if (value == null) {
            return true;
        }

        return !value.isAfter(LocalDateTime.now());
    }
}