package com.banking.api.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * Validator implementation for {@link ValidAccountNumber} annotation.
 * 
 * <p>This validator checks that an account number follows the format:
 * <b>ACC-XXXXX</b> where X is an alphanumeric character (A-Z or 0-9).
 * 
 * <p>The validation uses a regular expression pattern to ensure:
 * <ul>
 *   <li>Starts with "ACC-" (uppercase)</li>
 *   <li>Followed by exactly 5 uppercase letters or digits</li>
 *   <li>No additional characters before or after</li>
 * </ul>
 * 
 * <p><b>Regex Pattern:</b> {@code ^ACC-[A-Z0-9]{5}$}
 * 
 * @author Banking API Team
 * @version 1.0
 * @since 2026-01-22
 */
public class AccountNumberValidator implements ConstraintValidator<ValidAccountNumber, String> {
    
    /**
     * Regular expression pattern for account number validation.
     * Pattern: ACC- followed by exactly 5 alphanumeric characters (uppercase)
     */
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^ACC-[A-Z0-9]{5}$");
    
    /**
     * Initializes the validator.
     * 
     * @param constraintAnnotation The annotation instance
     */
    @Override
    public void initialize(ValidAccountNumber constraintAnnotation) {
        // No initialization needed
    }
    
    /**
     * Validates the account number format.
     * 
     * <p>Null or empty values are allowed (use @NotNull/@NotBlank for required fields).
     * This allows optional account fields like fromAccount in deposits.
     * 
     * @param accountNumber The account number to validate
     * @param context The constraint validator context
     * @return true if valid or null/empty, false if invalid format
     */
    @Override
    public boolean isValid(String accountNumber, ConstraintValidatorContext context) {
        // Null or empty values are allowed (optional fields)
        // Use @NotNull or @NotBlank if the field is required
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return true;
        }
        
        // Validate against the pattern
        boolean isValid = ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches();
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Account number must follow format ACC-XXXXX where X is an uppercase letter or digit"
            ).addConstraintViolation();
        }
        
        return isValid;
    }
}
