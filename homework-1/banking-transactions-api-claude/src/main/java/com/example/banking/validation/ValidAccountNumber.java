package com.example.banking.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AccountNumberValidator.class)
public @interface ValidAccountNumber {
    String message() default "Account number must follow format ACC-XXXXX (where X is alphanumeric)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
