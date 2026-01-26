package com.example.banking.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CurrencyValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private CurrencyValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CurrencyValidator();
    }

    @Test
    void isValid_withValidCurrencyUSD_returnsTrue() {
        assertThat(validator.isValid("USD", context)).isTrue();
    }

    @Test
    void isValid_withValidCurrencyEUR_returnsTrue() {
        assertThat(validator.isValid("EUR", context)).isTrue();
    }

    @Test
    void isValid_withValidCurrencyGBP_returnsTrue() {
        assertThat(validator.isValid("GBP", context)).isTrue();
    }

    @Test
    void isValid_withValidCurrencyJPY_returnsTrue() {
        assertThat(validator.isValid("JPY", context)).isTrue();
    }

    @Test
    void isValid_withLowercaseCurrency_returnsTrue() {
        assertThat(validator.isValid("usd", context)).isTrue();
    }

    @Test
    void isValid_withMixedCaseCurrency_returnsTrue() {
        assertThat(validator.isValid("EuR", context)).isTrue();
    }

    @Test
    void isValid_withInvalidCurrency_returnsFalse() {
        assertThat(validator.isValid("XYZ", context)).isFalse();
    }

    @Test
    void isValid_withInvalidCurrency_ABC_returnsFalse() {
        assertThat(validator.isValid("ABC", context)).isFalse();
    }

    @Test
    void isValid_withNull_returnsTrue() {
        // Null/blank validation is handled by @NotBlank
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    void isValid_withEmptyString_returnsTrue() {
        // Null/blank validation is handled by @NotBlank
        assertThat(validator.isValid("", context)).isTrue();
    }

    @Test
    void isValid_withBlankString_returnsTrue() {
        // Null/blank validation is handled by @NotBlank
        assertThat(validator.isValid("   ", context)).isTrue();
    }

    @Test
    void isValid_withTooShortCode_returnsFalse() {
        assertThat(validator.isValid("US", context)).isFalse();
    }

    @Test
    void isValid_withTooLongCode_returnsFalse() {
        assertThat(validator.isValid("USDD", context)).isFalse();
    }
}
