package com.bank.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CustomerIdTest {

    @Test
    void newIdGeneratesADistinctValueEachTime() {
        assertThat(CustomerId.newId().value()).isNotEqualTo(CustomerId.newId().value());
    }

    @Test
    void rejectsBlankValue() {
        assertThatThrownBy(() -> new CustomerId(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new CustomerId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
