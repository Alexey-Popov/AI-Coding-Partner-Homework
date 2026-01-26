package com.example.banking.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionStatusTest {

    @Test
    void fromValue_withPending_returnsPending() {
        assertThat(TransactionStatus.fromValue("pending")).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void fromValue_withCompleted_returnsCompleted() {
        assertThat(TransactionStatus.fromValue("completed")).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void fromValue_withFailed_returnsFailed() {
        assertThat(TransactionStatus.fromValue("failed")).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    void fromValue_withUppercase_returnsCorrectStatus() {
        assertThat(TransactionStatus.fromValue("PENDING")).isEqualTo(TransactionStatus.PENDING);
    }

    @Test
    void fromValue_withMixedCase_returnsCorrectStatus() {
        assertThat(TransactionStatus.fromValue("CoMpLeTeD")).isEqualTo(TransactionStatus.COMPLETED);
    }

    @Test
    void fromValue_withInvalidValue_throwsException() {
        assertThatThrownBy(() -> TransactionStatus.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid transaction status: invalid")
                .hasMessageContaining("Must be one of: pending, completed, failed");
    }

    @Test
    void fromValue_withNull_returnsNull() {
        assertThat(TransactionStatus.fromValue(null)).isNull();
    }

    @Test
    void getValue_returnsLowercaseValue() {
        assertThat(TransactionStatus.PENDING.getValue()).isEqualTo("pending");
        assertThat(TransactionStatus.COMPLETED.getValue()).isEqualTo("completed");
        assertThat(TransactionStatus.FAILED.getValue()).isEqualTo("failed");
    }
}
