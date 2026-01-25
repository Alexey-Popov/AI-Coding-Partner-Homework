package com.banking.transactions.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CurrencyValidator.class)
public @interface ValidCurrency {
    String message() default "Invalid currency code";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
