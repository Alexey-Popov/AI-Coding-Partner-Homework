package com.banking.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AmountValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAmount {
    
    String message() default "Amount must be positive with maximum 2 decimal places";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
