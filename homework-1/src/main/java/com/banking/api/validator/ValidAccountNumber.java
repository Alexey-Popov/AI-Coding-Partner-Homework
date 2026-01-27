package com.banking.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AccountNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAccountNumber {
    
    String message() default "Invalid account number format. Must be ACC-XXXXX where X is alphanumeric";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}
