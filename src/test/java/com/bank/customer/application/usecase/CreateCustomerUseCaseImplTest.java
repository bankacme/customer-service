package com.bank.customer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.customer.application.command.CreateCustomerCommand;
import com.bank.customer.application.usecase.fake.InMemoryCustomerEventPublisher;
import com.bank.customer.application.usecase.fake.InMemoryCustomerRepository;
import com.bank.customer.domain.event.CustomerCreated;
import com.bank.customer.domain.exception.DuplicateDocumentException;
import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import io.reactivex.rxjava3.observers.TestObserver;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class CreateCustomerUseCaseImplTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final InMemoryCustomerEventPublisher publisher = new InMemoryCustomerEventPublisher();
    private final CreateCustomerUseCaseImpl useCase =
            new CreateCustomerUseCaseImpl(repository, publisher, clock);

    private final CreateCustomerCommand command = new CreateCustomerCommand(
            CustomerType.PERSONAL, null, new CustomerName("Ana Torres"),
            new Document(DocumentType.DNI, "12345678"),
            new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), null));

    @Test
    void savesTheCustomerAndPublishesCustomerCreated() {
        TestObserver<com.bank.customer.domain.model.Customer> observer = useCase.execute(command).test();

        observer.assertComplete();
        observer.assertValueCount(1);
        assertThat(publisher.published()).hasSize(1);
        assertThat(publisher.published().get(0)).isInstanceOf(CustomerCreated.class);
    }

    @Test
    void rejectsADuplicateDocument() {
        useCase.execute(command).test().assertComplete();

        useCase.execute(command).test()
                .assertError(DuplicateDocumentException.class);
    }
}
