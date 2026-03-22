package com.example.banking.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AccountNumberValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private AccountNumberValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AccountNumberValidator();
    }

    @Test
    void isValid_withValidAccountNumber_numeric_returnsTrue() {
        assertThat(validator.isValid("ACC-12345", context)).isTrue();
    }

    @Test
    void isValid_withValidAccountNumber_alphanumeric_returnsTrue() {
        assertThat(validator.isValid("ACC-AB123", context)).isTrue();
    }

    @Test
    void isValid_withValidAccountNumber_allLetters_returnsTrue() {
        assertThat(validator.isValid("ACC-ABCDE", context)).isTrue();
    }

    @Test
    void isValid_withValidAccountNumber_mixedCase_returnsTrue() {
        assertThat(validator.isValid("ACC-AbC12", context)).isTrue();
    }

    @Test
    void isValid_withInvalidPrefix_returnsFalse() {
        assertThat(validator.isValid("BAD-12345", context)).isFalse();
    }

    @Test
    void isValid_withMissingPrefix_returnsFalse() {
        assertThat(validator.isValid("12345", context)).isFalse();
    }

    @Test
    void isValid_withTooShortSuffix_returnsFalse() {
        assertThat(validator.isValid("ACC-1234", context)).isFalse();
    }

    @Test
    void isValid_withTooLongSuffix_returnsFalse() {
        assertThat(validator.isValid("ACC-123456", context)).isFalse();
    }

    @Test
    void isValid_withSpecialCharacters_returnsFalse() {
        assertThat(validator.isValid("ACC-12@45", context)).isFalse();
    }

    @Test
    void isValid_withSpaces_returnsFalse() {
        assertThat(validator.isValid("ACC-12 45", context)).isFalse();
    }

    @Test
    void isValid_withNull_returnsTrue() {
        // Null should be handled by @NotNull annotation
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    void isValid_withEmptyString_returnsTrue() {
        // Empty should be handled by @NotBlank annotation
        assertThat(validator.isValid("", context)).isTrue();
    }

    @Test
    void isValid_withBlankString_returnsTrue() {
        // Blank should be handled by @NotBlank annotation
        assertThat(validator.isValid("   ", context)).isTrue();
    }

    @Test
    void isValid_withMissingDash_returnsFalse() {
        assertThat(validator.isValid("ACC12345", context)).isFalse();
    }

    @Test
    void isValid_withMultipleDashes_returnsFalse() {
        assertThat(validator.isValid("ACC-12-345", context)).isFalse();
    }

    @Test
    void isValid_withLowercasePrefix_returnsFalse() {
        assertThat(validator.isValid("acc-12345", context)).isFalse();
    }
}
