package com.bank.customer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

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
import com.bank.customer.domain.model.CustomerProfile;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class ChangeCustomerProfileUseCaseImplTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final InMemoryCustomerEventPublisher publisher = new InMemoryCustomerEventPublisher();
    private final ChangeCustomerProfileUseCaseImpl useCase =
            new ChangeCustomerProfileUseCaseImpl(repository, publisher, new NoOpCustomerCache(), clock);

    private Customer existingCustomer() {
        Customer customer = Customer.create(CustomerType.PERSONAL, null, new CustomerName("Ana Torres"),
                new Document(DocumentType.DNI, "12345678"),
                new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), null), clock);
        repository.save(customer).blockingGet();
        return customer;
    }

    @Test
    void changesTheProfileAndPublishesCustomerUpdated() {
        Customer customer = existingCustomer();

        Customer updated = useCase.execute(customer.id(), CustomerProfile.VIP).blockingGet();

        assertThat(updated.profile()).isEqualTo(CustomerProfile.VIP);
        assertThat(publisher.published()).hasSize(1);
        assertThat(publisher.published().get(0)).isInstanceOf(CustomerUpdated.class);
    }

    @Test
    void rejectsAnUnknownCustomerWithCustomerNotFound() {
        useCase.execute(new CustomerId("missing"), CustomerProfile.VIP).test()
                .assertError(CustomerNotFoundException.class);
    }

    @Test
    void rejectsAnIncompatibleProfileForTheCustomerType() {
        // Regla 3: PYME solo aplica a clientes BUSINESS; este cliente es PERSONAL.
        Customer customer = existingCustomer();

        useCase.execute(customer.id(), CustomerProfile.PYME).test()
                .assertError(error -> error instanceof InvalidCustomerException invalid
                        && "PROFILE_NOT_ALLOWED".equals(invalid.getErrorCode()));
        assertThat(publisher.published()).isEmpty();
    }

    @Test
    void rejectsChangingTheProfileOfAnInactiveCustomer() {
        Customer customer = existingCustomer().deactivate(clock);
        repository.save(customer).blockingGet();

        useCase.execute(customer.id(), CustomerProfile.VIP).test()
                .assertError(error -> error instanceof InvalidCustomerException invalid
                        && "CUSTOMER_INACTIVE".equals(invalid.getErrorCode()));
        assertThat(publisher.published()).isEmpty();
    }
}
