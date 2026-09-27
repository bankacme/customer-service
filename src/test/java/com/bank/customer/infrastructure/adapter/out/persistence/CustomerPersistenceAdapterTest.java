package com.bank.customer.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.bank.customer.application.port.in.CustomerFilter;
import com.bank.customer.domain.exception.DuplicateDocumentException;
import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerProfile;
import com.bank.customer.domain.model.CustomerStatus;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CustomerPersistenceAdapterTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);

    @Autowired
    private CustomerPersistenceAdapter adapter;

    @Autowired
    private CustomerMongoRepository repository;

    @BeforeEach
    @AfterEach
    void cleanCollection() {
        repository.deleteAll().blockingAwait();
    }

    @Test
    void savingTheSameDocumentTwiceTranslatesToDuplicateDocumentException() {
        Customer first = customer("Ana Torres", DocumentType.DNI, "12345678");
        adapter.save(first).blockingGet();

        Customer second = customer("Ana Torres Otra", DocumentType.DNI, "12345678");

        assertThatThrownBy(() -> adapter.save(second).blockingGet())
                .isInstanceOf(DuplicateDocumentException.class);
    }

    @Test
    void findAllCombinesTypeAndStatusFiltersEvenThoughMongoOnlyIndexesOneAtATime() {
        adapter.save(customer("Ana Torres", DocumentType.DNI, "11111111")).blockingGet();
        adapter.save(customer("Bodega San Martin", DocumentType.RUC, "20512345678")).blockingGet();
        adapter.save(customer("Beto Quispe", DocumentType.DNI, "22222222")).blockingGet();

        CustomerFilter personalActive = new CustomerFilter(CustomerType.PERSONAL, null, CustomerStatus.ACTIVE);

        assertThat(adapter.findAll(personalActive).toList().blockingGet()).hasSize(2);
    }

    @Test
    void existsAndFindByDocumentGoThroughTheMapper() {
        Customer customer = customer("Carla Mendoza", DocumentType.DNI, "34567890");
        adapter.save(customer).blockingGet();

        assertThat(adapter.existsByDocument(DocumentType.DNI, "34567890").blockingGet()).isTrue();
        assertThat(adapter.findByDocument(DocumentType.DNI, "34567890").blockingGet().name().value())
                .isEqualTo("Carla Mendoza");
    }

    private Customer customer(String name, DocumentType type, String number) {
        return Customer.create(type == DocumentType.RUC ? CustomerType.BUSINESS : CustomerType.PERSONAL,
                type == DocumentType.RUC ? CustomerProfile.PYME : null, new CustomerName(name),
                new Document(type, number),
                new ContactInfo(new Email(name.toLowerCase().replace(" ", ".") + "@bank.com"),
                        new PhoneNumber("987654321"), null),
                clock);
    }
}
