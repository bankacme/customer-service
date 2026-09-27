package com.bank.customer.infrastructure.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;

/**
 * Against the real local Mongo (same approach as bank-spike): exercises the derived
 * queries and confirms the unique index (uk_customer_document, created at startup by
 * CustomerIndexInitializer) actually rejects a duplicate (document.type, document.number).
 */
@SpringBootTest
class CustomerMongoRepositoryTest {

    @Autowired
    private CustomerMongoRepository repository;

    @BeforeEach
    @AfterEach
    void cleanCollection() {
        repository.deleteAll().blockingAwait();
    }

    @Test
    void existsAndFindByDocumentAgreeWithWhatWasSaved() {
        CustomerDocument saved = repository.save(sampleDocument("id-1", "DNI", "12345678")).blockingGet();

        assertThat(repository.existsByDocumentTypeAndDocumentNumber("DNI", "12345678").blockingGet()).isTrue();
        assertThat(repository.existsByDocumentTypeAndDocumentNumber("DNI", "99999999").blockingGet()).isFalse();
        assertThat(repository.findByDocumentTypeAndDocumentNumber("DNI", "12345678").blockingGet().getId())
                .isEqualTo(saved.getId());
    }

    @Test
    void findByStatusTypeAndProfileFilterIndependently() {
        repository.save(sampleDocument("id-2", "DNI", "11111111")).blockingGet();
        repository.save(sampleDocument("id-3", "DNI", "22222222")).blockingGet();

        assertThat(repository.findByStatus("ACTIVE").toList().blockingGet()).hasSize(2);
        assertThat(repository.findByType("PERSONAL").toList().blockingGet()).hasSize(2);
        assertThat(repository.findByProfile("STANDARD").toList().blockingGet()).hasSize(2);
    }

    @Test
    void theUniqueIndexRejectsADuplicateDocument() {
        repository.save(sampleDocument("id-4", "DNI", "33333333")).blockingGet();

        assertThatThrownBy(() -> repository.save(sampleDocument("id-5", "DNI", "33333333")).blockingGet())
                .isInstanceOf(DuplicateKeyException.class);
    }

    private CustomerDocument sampleDocument(String id, String documentType, String documentNumber) {
        Instant now = Instant.parse("2026-09-27T10:00:00Z");
        return CustomerDocument.builder()
                .id(id)
                .type("PERSONAL")
                .profile("STANDARD")
                .name("Ana Torres")
                .document(DocumentData.builder().type(documentType).number(documentNumber).build())
                .contact(ContactData.builder().email("ana@bank.com").phone("987654321").build())
                .status("ACTIVE")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
