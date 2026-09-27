package com.bank.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PhoneNumberTest {

    @Test
    void acceptsNineDigitsStartingWithNine() {
        assertThat(new PhoneNumber("987654321").value()).isEqualTo("987654321");
    }

    @Test
    void rejectsNumberNotStartingWithNine() {
        assertThatThrownBy(() -> new PhoneNumber("187654321"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsWrongLength() {
        assertThatThrownBy(() -> new PhoneNumber("98765432"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
