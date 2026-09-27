package com.bank.customer.infrastructure.adapter.in.rest;

import com.bank.customer.application.port.in.ChangeCustomerProfileUseCase;
import com.bank.customer.application.port.in.CreateCustomerUseCase;
import com.bank.customer.application.port.in.DeleteCustomerUseCase;
import com.bank.customer.application.port.in.FindAllCustomersUseCase;
import com.bank.customer.application.port.in.FindCustomerByDocumentUseCase;
import com.bank.customer.application.port.in.FindCustomerByIdUseCase;
import com.bank.customer.application.port.in.UpdateCustomerUseCase;
import com.bank.customer.domain.model.CustomerId;
import com.bank.customer.infrastructure.adapter.in.rest.api.CustomersApi;
import com.bank.customer.infrastructure.adapter.in.rest.dto.ChangeProfileRequest;
import com.bank.customer.infrastructure.adapter.in.rest.dto.CreateCustomerRequest;
import com.bank.customer.infrastructure.adapter.in.rest.dto.Customer;
import com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerProfile;
import com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerStatus;
import com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerType;
import com.bank.customer.infrastructure.adapter.in.rest.dto.DocumentType;
import com.bank.customer.infrastructure.adapter.in.rest.dto.UpdateCustomerRequest;
import com.bank.customer.infrastructure.mapper.CustomerRestMapper;
import com.bank.customer.infrastructure.support.RxJavaReactorBridge;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CustomerController implements CustomersApi {

    private final CreateCustomerUseCase createCustomerUseCase;
    private final FindCustomerByIdUseCase findCustomerByIdUseCase;
    private final FindAllCustomersUseCase findAllCustomersUseCase;
    private final FindCustomerByDocumentUseCase findCustomerByDocumentUseCase;
    private final UpdateCustomerUseCase updateCustomerUseCase;
    private final ChangeCustomerProfileUseCase changeCustomerProfileUseCase;
    private final DeleteCustomerUseCase deleteCustomerUseCase;
    private final CustomerRestMapper mapper;

    public CustomerController(CreateCustomerUseCase createCustomerUseCase,
                               FindCustomerByIdUseCase findCustomerByIdUseCase,
                               FindAllCustomersUseCase findAllCustomersUseCase,
                               FindCustomerByDocumentUseCase findCustomerByDocumentUseCase,
                               UpdateCustomerUseCase updateCustomerUseCase,
                               ChangeCustomerProfileUseCase changeCustomerProfileUseCase,
                               DeleteCustomerUseCase deleteCustomerUseCase,
                               CustomerRestMapper mapper) {
        this.createCustomerUseCase = createCustomerUseCase;
        this.findCustomerByIdUseCase = findCustomerByIdUseCase;
        this.findAllCustomersUseCase = findAllCustomersUseCase;
        this.findCustomerByDocumentUseCase = findCustomerByDocumentUseCase;
        this.updateCustomerUseCase = updateCustomerUseCase;
        this.changeCustomerProfileUseCase = changeCustomerProfileUseCase;
        this.deleteCustomerUseCase = deleteCustomerUseCase;
        this.mapper = mapper;
    }

    @Override
    public Mono<ResponseEntity<Customer>> createCustomer(Mono<CreateCustomerRequest> createCustomerRequest,
                                                           ServerWebExchange exchange) {
        return createCustomerRequest
                .map(mapper::toCommand)
                .flatMap(command -> RxJavaReactorBridge.toMono(createCustomerUseCase.execute(command)))
                .map(mapper::toDto)
                .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
    }

    @Override
    public Mono<ResponseEntity<Flux<Customer>>> listCustomers(CustomerType type, CustomerProfile profile,
                                                                CustomerStatus status, ServerWebExchange exchange) {
        Flux<Customer> customers = RxJavaReactorBridge
                .toFlux(findAllCustomersUseCase.execute(mapper.toFilter(type, profile, status)))
                .map(mapper::toDto);
        return Mono.just(ResponseEntity.ok(customers));
    }

    @Override
    public Mono<ResponseEntity<Customer>> getCustomer(String id, ServerWebExchange exchange) {
        return RxJavaReactorBridge.toMono(findCustomerByIdUseCase.execute(new CustomerId(id)))
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Customer>> getCustomerByDocument(DocumentType documentType, String documentNumber,
                                                                  ServerWebExchange exchange) {
        return RxJavaReactorBridge.toMono(findCustomerByDocumentUseCase
                        .execute(mapper.toDocumentType(documentType), documentNumber))
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Customer>> updateCustomer(String id, Mono<UpdateCustomerRequest> updateCustomerRequest,
                                                           ServerWebExchange exchange) {
        return updateCustomerRequest
                .map(mapper::toCommand)
                .flatMap(command -> RxJavaReactorBridge
                        .toMono(updateCustomerUseCase.execute(new CustomerId(id), command)))
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Customer>> changeCustomerProfile(String id,
                                                                  Mono<ChangeProfileRequest> changeProfileRequest,
                                                                  ServerWebExchange exchange) {
        return changeProfileRequest
                .map(mapper::toProfile)
                .flatMap(newProfile -> RxJavaReactorBridge
                        .toMono(changeCustomerProfileUseCase.execute(new CustomerId(id), newProfile)))
                .map(mapper::toDto)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(String id, ServerWebExchange exchange) {
        return RxJavaReactorBridge.toMono(deleteCustomerUseCase.execute(new CustomerId(id)))
                .thenReturn(ResponseEntity.noContent().build());
    }
}
