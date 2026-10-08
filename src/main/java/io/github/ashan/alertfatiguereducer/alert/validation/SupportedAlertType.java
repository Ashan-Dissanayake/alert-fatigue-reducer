package io.github.ashan.alertfatiguereducer.alert.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SupportedAlertTypeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface SupportedAlertType {

    String message() default "unsupported alert type";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}