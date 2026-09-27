package com.bank.customer.domain.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CustomerEventsTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-25T10:00:00Z"), ZoneOffset.UTC);
    private final Customer customer = Customer.create(CustomerType.PERSONAL, null,
            new CustomerName("Ana Torres"), new Document(DocumentType.DNI, "12345678"),
            new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), null), clock);

    @Test
    void customerCreatedCarriesTheFullCurrentState() {
        CustomerCreated event = CustomerCreated.from(customer);

        assertThat(event.customerId()).isEqualTo(customer.id().value());
        assertThat(event.type()).isEqualTo("PERSONAL");
        assertThat(event.profile()).isEqualTo("STANDARD");
        assertThat(event.status()).isEqualTo("ACTIVE");
        assertThat(event.document()).isEqualTo(new DocumentSnapshot("DNI", "12345678"));
        assertThat(event.occurredAt()).isEqualTo(customer.updatedAt());
    }

    @Test
    void customerUpdatedReflectsTheNewState() {
        Customer updated = customer.update(new CustomerName("Ana Torres Lopez"),
                customer.contact(), Clock.fixed(clock.instant().plusSeconds(60), ZoneOffset.UTC));

        CustomerUpdated event = CustomerUpdated.from(updated);

        assertThat(event.name()).isEqualTo("Ana Torres Lopez");
        assertThat(event.occurredAt()).isEqualTo(updated.updatedAt());
    }

    @Test
    void customerDeletedReflectsInactiveStatus() {
        Customer deactivated = customer.deactivate(clock);

        CustomerDeleted event = CustomerDeleted.from(deactivated);

        assertThat(event.status()).isEqualTo("INACTIVE");
    }
}
