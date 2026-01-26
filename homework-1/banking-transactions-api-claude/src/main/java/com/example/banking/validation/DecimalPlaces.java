package com.example.banking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DecimalPlacesValidator.class)
public @interface DecimalPlaces {
    String message() default "Amount must have at most {max} decimal places";
    int max() default 2;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
