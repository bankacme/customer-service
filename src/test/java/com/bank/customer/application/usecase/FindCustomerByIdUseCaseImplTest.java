package com.bank.customer.application.usecase;

import com.bank.customer.application.usecase.fake.InMemoryCustomerRepository;
import com.bank.customer.application.usecase.fake.NoOpCustomerCache;
import com.bank.customer.domain.exception.CustomerNotFoundException;
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

class FindCustomerByIdUseCaseImplTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final FindCustomerByIdUseCaseImpl useCase =
            new FindCustomerByIdUseCaseImpl(repository, new NoOpCustomerCache());

    @Test
    void returnsTheCustomerWhenItExists() {
        Customer customer = Customer.create(CustomerType.PERSONAL, null, new CustomerName("Ana Torres"),
                new Document(DocumentType.DNI, "12345678"),
                new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), null), clock);
        repository.save(customer).blockingGet();

        useCase.execute(customer.id()).test()
                .assertComplete()
                .assertValue(customer);
    }

    @Test
    void failsWithCustomerNotFoundWhenItDoesNotExist() {
        useCase.execute(new CustomerId("does-not-exist")).test()
                .assertError(CustomerNotFoundException.class);
    }
}
