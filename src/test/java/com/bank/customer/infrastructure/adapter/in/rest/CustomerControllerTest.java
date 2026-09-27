package com.bank.customer.infrastructure.adapter.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.bank.customer.application.command.CreateCustomerCommand;
import com.bank.customer.application.port.in.ChangeCustomerProfileUseCase;
import com.bank.customer.application.port.in.CreateCustomerUseCase;
import com.bank.customer.application.port.in.DeleteCustomerUseCase;
import com.bank.customer.application.port.in.FindAllCustomersUseCase;
import com.bank.customer.application.port.in.FindCustomerByDocumentUseCase;
import com.bank.customer.application.port.in.FindCustomerByIdUseCase;
import com.bank.customer.application.port.in.UpdateCustomerUseCase;
import com.bank.customer.domain.exception.CustomerNotFoundException;
import com.bank.customer.domain.exception.DuplicateDocumentException;
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
import com.bank.customer.infrastructure.mapper.CustomerRestMapper;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest(CustomerController.class)
@Import(CustomerRestMapper.class)
class CustomerControllerTest {

    @Autowired
    private WebTestClient client;

    @MockitoBean
    private CreateCustomerUseCase createCustomerUseCase;
    @MockitoBean
    private FindCustomerByIdUseCase findCustomerByIdUseCase;
    @MockitoBean
    private FindAllCustomersUseCase findAllCustomersUseCase;
    @MockitoBean
    private FindCustomerByDocumentUseCase findCustomerByDocumentUseCase;
    @MockitoBean
    private UpdateCustomerUseCase updateCustomerUseCase;
    @MockitoBean
    private ChangeCustomerProfileUseCase changeCustomerProfileUseCase;
    @MockitoBean
    private DeleteCustomerUseCase deleteCustomerUseCase;

    private final Clock clock = Clock.fixed(Instant.parse("2026-09-27T10:00:00Z"), ZoneOffset.UTC);
    private final Customer sampleCustomer = Customer.create(CustomerType.PERSONAL, null,
            new CustomerName("Ana Torres"), new Document(DocumentType.DNI, "12345678"),
            new ContactInfo(new Email("ana@bank.com"), new PhoneNumber("987654321"), null), clock);

    @Test
    void createCustomerReturns201WithTheCreatedCustomer() {
        given(createCustomerUseCase.execute(any(CreateCustomerCommand.class))).willReturn(Single.just(sampleCustomer));

        client.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "type": "PERSONAL",
                          "name": "Ana Torres",
                          "document": { "type": "DNI", "number": "12345678" },
                          "contact": { "email": "ana@bank.com", "phone": "987654321" }
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(sampleCustomer.id().value())
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    void createCustomerWithAMissingRequiredFieldReturns400ValidationError() {
        client.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Ana Torres",
                          "document": { "type": "DNI", "number": "12345678" },
                          "contact": { "email": "ana@bank.com", "phone": "987654321" }
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.code").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void createCustomerWithADuplicateDocumentReturns409() {
        given(createCustomerUseCase.execute(any(CreateCustomerCommand.class)))
                .willReturn(Single.error(new DuplicateDocumentException("DNI", "12345678")));

        client.post().uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "type": "PERSONAL",
                          "name": "Ana Torres",
                          "document": { "type": "DNI", "number": "12345678" },
                          "contact": { "email": "ana@bank.com", "phone": "987654321" }
                        }
                        """)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.code").isEqualTo("DOCUMENT_ALREADY_REGISTERED");
    }

    @Test
    void getCustomerThatDoesNotExistReturns404() {
        given(findCustomerByIdUseCase.execute(eq(new CustomerId("missing"))))
                .willReturn(Single.error(new CustomerNotFoundException("missing")));

        client.get().uri("/api/v1/customers/missing")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.code").isEqualTo("CUSTOMER_NOT_FOUND");
    }

    @Test
    void changeProfileViolatingBusinessRuleReturns422WithItsSpecificCode() {
        given(changeCustomerProfileUseCase.execute(eq(sampleCustomer.id()), eq(CustomerProfile.PYME)))
                .willReturn(Single.error(new InvalidCustomerException("PROFILE_NOT_ALLOWED", "not allowed")));

        client.patch().uri("/api/v1/customers/{id}/profile", sampleCustomer.id().value())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{ \"profile\": \"PYME\" }")
                .exchange()
                .expectStatus().isEqualTo(422)
                .expectBody()
                .jsonPath("$.code").isEqualTo("PROFILE_NOT_ALLOWED");
    }

    @Test
    void deleteCustomerReturns204WithNoBody() {
        given(deleteCustomerUseCase.execute(sampleCustomer.id())).willReturn(Completable.complete());

        client.delete().uri("/api/v1/customers/{id}", sampleCustomer.id().value())
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    void listCustomersReturnsWhateverTheUseCaseStreams() {
        given(findAllCustomersUseCase.execute(any())).willReturn(Flowable.just(sampleCustomer));

        client.get().uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(Object.class).hasSize(1);
    }
}
