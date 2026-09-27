package com.bank.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ContactInfoTest {

    private final Email email = new Email("ana@bank.com");
    private final PhoneNumber phone = new PhoneNumber("987654321");

    @Test
    void addressIsOptional() {
        assertThat(new ContactInfo(email, phone, null).address()).isNull();
    }

    @Test
    void blankAddressBecomesNull() {
        assertThat(new ContactInfo(email, phone, "   ").address()).isNull();
    }

    @Test
    void trimsAddress() {
        assertThat(new ContactInfo(email, phone, "  Av. Siempre Viva 123  ").address())
                .isEqualTo("Av. Siempre Viva 123");
    }

    @Test
    void rejectsAddressLongerThan200Characters() {
        String tooLong = "A".repeat(201);
        assertThatThrownBy(() -> new ContactInfo(email, phone, tooLong))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresEmail() {
        assertThatThrownBy(() -> new ContactInfo(null, phone, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresPhone() {
        assertThatThrownBy(() -> new ContactInfo(email, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
