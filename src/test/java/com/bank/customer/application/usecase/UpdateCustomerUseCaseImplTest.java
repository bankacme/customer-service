package com.bank.customer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.customer.application.command.UpdateCustomerCommand;
import com.bank.customer.application.usecase.fake.InMemoryCustomerEventPublisher;
import com.bank.customer.application.usecase.fake.InMemoryCustomerRepository;
import com.bank.customer.application.usecase.fake.NoOpCustomerCache;
import com.bank.customer.domain.event.CustomerUpdated;
import com.bank.customer.domain.exception.CustomerNotFoundException;
import com.bank.customer.domain.exception.InvalidCustomerException;
import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class UpdateCustomerUseCaseImplTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final InMemoryCustomerEventPublisher publisher = new InMemoryCustomerEventPublisher();
    private final UpdateCustomerUseCaseImpl useCase =
            new UpdateCustomerUseCaseImpl(repository, publisher, new NoOpCustomerCache(), clock);

    private Customer existingCustomer() {
        Customer customer = Customer.create(CustomerType.PERSONAL, null, new CustomerName("Ana Torres"),
                new Document(DocumentType.DNI, "12345678"),
                new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), null), clock);
        repository.save(customer).blockingGet();
        return customer;
    }

    @Test
    void updatesNameAndContactAndPublishesCustomerUpdated() {
        Customer customer = existingCustomer();
        UpdateCustomerCommand command = new UpdateCustomerCommand(new CustomerName("Ana Torres Rojas"),
                new ContactInfo(new Email("ana.rojas@bank.com"), new PhoneNumber("911222333"), "Av. Siempre Viva"));

        Customer updated = useCase.execute(customer.id(), command).blockingGet();

        assertThat(updated.name().value()).isEqualTo("Ana Torres Rojas");
        assertThat(updated.contact().email().value()).isEqualTo("ana.rojas@bank.com");
        assertThat(publisher.published()).hasSize(1);
        assertThat(publisher.published().get(0)).isInstanceOf(CustomerUpdated.class);
    }

    @Test
    void rejectsAnUnknownCustomerWithCustomerNotFound() {
        UpdateCustomerCommand command = new UpdateCustomerCommand(new CustomerName("Alguien"),
                new ContactInfo(new Email("alguien@bank.com"), new PhoneNumber("911222333"), null));

        useCase.execute(new CustomerId("missing"), command).test()
                .assertError(CustomerNotFoundException.class);
    }

    @Test
    void rejectsUpdatingAnInactiveCustomer() {
        Customer customer = existingCustomer().deactivate(clock);
        repository.save(customer).blockingGet();
        UpdateCustomerCommand command = new UpdateCustomerCommand(new CustomerName("Alguien"),
                new ContactInfo(new Email("alguien@bank.com"), new PhoneNumber("911222333"), null));

        useCase.execute(customer.id(), command).test()
                .assertError(InvalidCustomerException.class);
        assertThat(publisher.published()).isEmpty();
    }
}
