package com.bank.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CustomerNameTest {

    @Test
    void trimsSurroundingWhitespace() {
        assertThat(new CustomerName("  Ana Torres  ").value()).isEqualTo("Ana Torres");
    }

    @Test
    void rejectsBlank() {
        assertThatThrownBy(() -> new CustomerName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsLongerThan150Characters() {
        String tooLong = "A".repeat(151);
        assertThatThrownBy(() -> new CustomerName(tooLong))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void acceptsExactly150Characters() {
        String limit = "A".repeat(150);
        assertThat(new CustomerName(limit).value()).hasSize(150);
    }
}
