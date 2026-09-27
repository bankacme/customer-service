package com.bank.customer.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bank.customer.domain.exception.InvalidCustomerException;
import org.junit.jupiter.api.Test;

class DocumentTest {

    @Test
    void acceptsAnEightDigitDni() {
        assertThat(new Document(DocumentType.DNI, "12345678").number()).isEqualTo("12345678");
    }

    @Test
    void rejectsADniWithFewerThanEightDigits() {
        assertThatThrownBy(() -> new Document(DocumentType.DNI, "1234567"))
                .isInstanceOf(InvalidCustomerException.class)
                .satisfies(ex -> assertThat(((InvalidCustomerException) ex).getErrorCode())
                        .isEqualTo("INVALID_DOCUMENT"));
    }

    @Test
    void acceptsCexBetweenNineAndTwelveAlphanumericChars() {
        assertThat(new Document(DocumentType.CEX, "ABC123456").number()).isEqualTo("ABC123456");
    }

    @Test
    void rejectsCexShorterThanNineChars() {
        assertThatThrownBy(() -> new Document(DocumentType.CEX, "ABC1234"))
                .isInstanceOf(InvalidCustomerException.class);
    }

    @Test
    void acceptsPassportBetweenSixAndTwelveAlphanumericChars() {
        assertThat(new Document(DocumentType.PASSPORT, "AB1234").number()).isEqualTo("AB1234");
    }

    @Test
    void acceptsAnElevenDigitRuc() {
        assertThat(new Document(DocumentType.RUC, "20123456789").number()).isEqualTo("20123456789");
    }

    @Test
    void rejectsARucWithLetters() {
        assertThatThrownBy(() -> new Document(DocumentType.RUC, "2012345678A"))
                .isInstanceOf(InvalidCustomerException.class);
    }

    @Test
    void rejectsNullType() {
        assertThatThrownBy(() -> new Document(null, "12345678"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
