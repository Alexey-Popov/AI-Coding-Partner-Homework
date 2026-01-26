package com.banking.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrencyCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCurrency {
    
    String message() default "Invalid currency code";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
