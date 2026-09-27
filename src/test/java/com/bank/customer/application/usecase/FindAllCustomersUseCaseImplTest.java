package com.bank.customer.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import com.bank.customer.application.port.in.CustomerFilter;
import com.bank.customer.application.usecase.fake.InMemoryCustomerRepository;
import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.Customer;
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

class FindAllCustomersUseCaseImplTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);
    private final InMemoryCustomerRepository repository = new InMemoryCustomerRepository();
    private final FindAllCustomersUseCaseImpl useCase = new FindAllCustomersUseCaseImpl(repository);

    @Test
    void withNoFilterReturnsEveryCustomer() {
        repository.save(customer(CustomerType.PERSONAL, null, "11111111")).blockingGet();
        repository.save(customer(CustomerType.BUSINESS, CustomerProfile.PYME, "20512345678")).blockingGet();

        assertThat(useCase.execute(CustomerFilter.none()).toList().blockingGet()).hasSize(2);
    }

    @Test
    void filtersByTypeDelegatingToTheRepository() {
        repository.save(customer(CustomerType.PERSONAL, null, "22222222")).blockingGet();
        repository.save(customer(CustomerType.BUSINESS, CustomerProfile.PYME, "20512345679")).blockingGet();

        CustomerFilter onlyPersonal = new CustomerFilter(CustomerType.PERSONAL, null, null);

        assertThat(useCase.execute(onlyPersonal).toList().blockingGet()).hasSize(1);
    }

    private Customer customer(CustomerType type, CustomerProfile profile, String documentNumber) {
        DocumentType documentType = type == CustomerType.BUSINESS ? DocumentType.RUC : DocumentType.DNI;
        return Customer.create(type, profile, new CustomerName("Sample " + documentNumber),
                new Document(documentType, documentNumber),
                new ContactInfo(new Email("sample" + documentNumber + "@bank.com"),
                        new PhoneNumber("987654321"), null),
                clock);
    }
}
