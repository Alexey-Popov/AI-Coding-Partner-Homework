package com.example.banking.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecimalPlacesValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private DecimalPlaces decimalPlaces;

    private DecimalPlacesValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DecimalPlacesValidator();
        when(decimalPlaces.max()).thenReturn(2);
        validator.initialize(decimalPlaces);
    }

    @Test
    void isValid_withNoDecimalPlaces_returnsTrue() {
        assertThat(validator.isValid(new BigDecimal("100"), context)).isTrue();
    }

    @Test
    void isValid_withOneDecimalPlace_returnsTrue() {
        assertThat(validator.isValid(new BigDecimal("100.5"), context)).isTrue();
    }

    @Test
    void isValid_withTwoDecimalPlaces_returnsTrue() {
        assertThat(validator.isValid(new BigDecimal("100.50"), context)).isTrue();
    }

    @Test
    void isValid_withExactlyTwoDecimalPlaces_returnsTrue() {
        assertThat(validator.isValid(new BigDecimal("99.99"), context)).isTrue();
    }

    @Test
    void isValid_withThreeDecimalPlaces_returnsFalse() {
        assertThat(validator.isValid(new BigDecimal("100.123"), context)).isFalse();
    }

    @Test
    void isValid_withFourDecimalPlaces_returnsFalse() {
        assertThat(validator.isValid(new BigDecimal("100.1234"), context)).isFalse();
    }

    @Test
    void isValid_withTrailingZeros_returnsTrue() {
        // BigDecimal.stripTrailingZeros() should handle this
        assertThat(validator.isValid(new BigDecimal("100.500"), context)).isTrue();
    }

    @Test
    void isValid_withNull_returnsTrue() {
        // Null should be handled by @NotNull annotation
        assertThat(validator.isValid(null, context)).isTrue();
    }

    @Test
    void isValid_withZero_returnsTrue() {
        assertThat(validator.isValid(new BigDecimal("0"), context)).isTrue();
    }

    @Test
    void isValid_withZeroPointZero_returnsTrue() {
        assertThat(validator.isValid(new BigDecimal("0.0"), context)).isTrue();
    }

    @Test
    void isValid_withVerySmallNumber_twoDecimals_returnsTrue() {
        assertThat(validator.isValid(new BigDecimal("0.01"), context)).isTrue();
    }

    @Test
    void isValid_withVerySmallNumber_threeDecimals_returnsFalse() {
        assertThat(validator.isValid(new BigDecimal("0.001"), context)).isFalse();
    }

    @Test
    void isValid_withLargeNumber_twoDecimals_returnsTrue() {
        assertThat(validator.isValid(new BigDecimal("999999.99"), context)).isTrue();
    }

    @Test
    void isValid_withLargeNumber_threeDecimals_returnsFalse() {
        assertThat(validator.isValid(new BigDecimal("999999.999"), context)).isFalse();
    }

    @Test
    void isValid_withScientificNotation_returnsTrue() {
        // 1E+3 = 1000 (no decimal places)
        assertThat(validator.isValid(new BigDecimal("1E+3"), context)).isTrue();
    }

    @Test
    void isValid_withNegativeScale_returnsTrue() {
        // Numbers like 1000 have negative scale after stripTrailingZeros
        assertThat(validator.isValid(new BigDecimal("1000"), context)).isTrue();
    }
}
