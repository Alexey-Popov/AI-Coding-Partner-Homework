package com.banking.transactions.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DecimalPlacesValidator.class)
public @interface ValidDecimalPlaces {
    String message() default "Number must have maximum {max} decimal places";
    int max() default 2;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
