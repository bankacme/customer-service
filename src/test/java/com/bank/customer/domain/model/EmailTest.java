package com.bank.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void lowercasesAndTrims() {
        assertThat(new Email("  Ana.Torres@Bank.COM  ").value()).isEqualTo("ana.torres@bank.com");
    }

    @Test
    void rejectsMissingAtSign() {
        assertThatThrownBy(() -> new Email("ana.torres-bank.com"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new Email(""))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLongerThan254Characters() {
        String tooLong = "a".repeat(250) + "@a.co";
        assertThatThrownBy(() -> new Email(tooLong))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
