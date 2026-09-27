package com.bank.customer.infrastructure.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import com.bank.customer.infrastructure.adapter.out.persistence.CustomerDocument;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CustomerDocumentMapperTest {

    private final CustomerDocumentMapper mapper = new CustomerDocumentMapper();
    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void roundTripsAllFieldsIncludingOptionalAddress() {
        Customer customer = Customer.create(CustomerType.PERSONAL, null, new CustomerName("Ana Torres"),
                new Document(DocumentType.DNI, "12345678"),
                new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), "Av. Los Olivos 123"),
                clock);

        CustomerDocument document = mapper.toDocument(customer);
        Customer roundTripped = mapper.toDomain(document);

        assertThat(document.getId()).isEqualTo(customer.id().value());
        assertThat(document.getDocument().getType()).isEqualTo("DNI");
        assertThat(document.getContact().getAddress()).isEqualTo("Av. Los Olivos 123");
        assertThat(roundTripped).isEqualTo(customer);
    }

    @Test
    void roundTripsWithoutAnAddress() {
        Customer customer = Customer.create(CustomerType.BUSINESS, null, new CustomerName("Bodega San Martin"),
                new Document(DocumentType.RUC, "20512345678"),
                new ContactInfo(new Email("bodega@bank.com"), new PhoneNumber("912345678"), null), clock);

        CustomerDocument document = mapper.toDocument(customer);

        assertThat(document.getContact().getAddress()).isNull();
        assertThat(mapper.toDomain(document)).isEqualTo(customer);
    }
}
