package com.example.banking.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTypeTest {

    @Test
    void fromValue_withDeposit_returnsDeposit() {
        assertThat(TransactionType.fromValue("deposit")).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void fromValue_withWithdrawal_returnsWithdrawal() {
        assertThat(TransactionType.fromValue("withdrawal")).isEqualTo(TransactionType.WITHDRAWAL);
    }

    @Test
    void fromValue_withTransfer_returnsTransfer() {
        assertThat(TransactionType.fromValue("transfer")).isEqualTo(TransactionType.TRANSFER);
    }

    @Test
    void fromValue_withUppercase_returnsCorrectType() {
        assertThat(TransactionType.fromValue("DEPOSIT")).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void fromValue_withMixedCase_returnsCorrectType() {
        assertThat(TransactionType.fromValue("DePosIt")).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    void fromValue_withInvalidValue_throwsException() {
        assertThatThrownBy(() -> TransactionType.fromValue("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid transaction type: invalid")
                .hasMessageContaining("Must be one of: deposit, withdrawal, transfer");
    }

    @Test
    void fromValue_withNull_returnsNull() {
        assertThat(TransactionType.fromValue(null)).isNull();
    }

    @Test
    void getValue_returnsLowercaseValue() {
        assertThat(TransactionType.DEPOSIT.getValue()).isEqualTo("deposit");
        assertThat(TransactionType.WITHDRAWAL.getValue()).isEqualTo("withdrawal");
        assertThat(TransactionType.TRANSFER.getValue()).isEqualTo("transfer");
    }
}
