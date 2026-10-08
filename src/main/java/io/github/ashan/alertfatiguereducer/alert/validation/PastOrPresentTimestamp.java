package io.github.ashan.alertfatiguereducer.alert.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PastOrPresentTimestampValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface PastOrPresentTimestamp {

    String message() default "timestamp cannot be in the future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}