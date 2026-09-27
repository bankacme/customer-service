package com.bank.customer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.customer.application.usecase.fake.InMemoryCustomerEventPublisher;
import com.bank.customer.application.usecase.fake.InMemoryCustomerRepository;
import com.bank.customer.application.usecase.fake.NoOpCustomerCache;
import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerStatus;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class DeleteCustomerUseCaseImplTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final InMemoryCustomerEventPublisher publisher = new InMemoryCustomerEventPublisher();
    private final DeleteCustomerUseCaseImpl useCase =
            new DeleteCustomerUseCaseImpl(repository, publisher, new NoOpCustomerCache(), clock);

    @Test
    void deactivatesTheCustomerAndPublishesCustomerDeleted() {
        Customer customer = Customer.create(CustomerType.PERSONAL, null, new CustomerName("Ana Torres"),
                new Document(DocumentType.DNI, "12345678"),
                new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), null), clock);
        repository.save(customer).blockingGet();

        useCase.execute(customer.id()).test().assertComplete();

        assertThat(repository.findById(customer.id()).blockingGet().status()).isEqualTo(CustomerStatus.INACTIVE);
        assertThat(publisher.published()).hasSize(1);
    }

    @Test
    void repeatingTheDeleteIsIdempotentAndDoesNotPublishAgain() {
        // Per data-model.md section 6: "customer.deleted ... solo la primera vez; repetir la baja no publica".
        Customer customer = Customer.create(CustomerType.PERSONAL, null, new CustomerName("Ana Torres"),
                new Document(DocumentType.DNI, "12345678"),
                new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), null), clock);
        repository.save(customer).blockingGet();

        useCase.execute(customer.id()).test().assertComplete();
        useCase.execute(customer.id()).test().assertComplete();

        assertThat(publisher.published()).hasSize(1);
    }
}
