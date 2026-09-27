package com.bank.customer.infrastructure.adapter.out.persistence;

import com.bank.customer.application.port.in.CustomerFilter;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.domain.exception.DuplicateDocumentException;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.infrastructure.mapper.CustomerDocumentMapper;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

@Component
public class CustomerPersistenceAdapter implements CustomerRepositoryPort {

    private final CustomerMongoRepository repository;
    private final CustomerDocumentMapper mapper;

    public CustomerPersistenceAdapter(CustomerMongoRepository repository, CustomerDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Single<Customer> save(Customer customer) {
        return repository.save(mapper.toDocument(customer))
                .map(mapper::toDomain)
                .onErrorResumeNext(error -> error instanceof DuplicateKeyException
                        ? Single.error(new DuplicateDocumentException(
                                customer.document().type().name(), customer.document().number()))
                        : Single.error(error));
    }

    @Override
    public Maybe<Customer> findById(CustomerId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public Flowable<Customer> findAll(CustomerFilter filter) {
        Flowable<CustomerDocument> base;
        if (filter.status() != null) {
            base = repository.findByStatus(filter.status().name());
        } else if (filter.type() != null) {
            base = repository.findByType(filter.type().name());
        } else if (filter.profile() != null) {
            base = repository.findByProfile(filter.profile().name());
        } else {
            base = repository.findAll();
        }

        return base.map(mapper::toDomain)
                .filter(customer -> filter.type() == null || customer.type() == filter.type())
                .filter(customer -> filter.profile() == null || customer.profile() == filter.profile())
                .filter(customer -> filter.status() == null || customer.status() == filter.status());
    }

    @Override
    public Maybe<Customer> findByDocument(DocumentType type, String number) {
        return repository.findByDocumentTypeAndDocumentNumber(type.name(), number).map(mapper::toDomain);
    }

    @Override
    public Single<Boolean> existsByDocument(DocumentType type, String number) {
        return repository.existsByDocumentTypeAndDocumentNumber(type.name(), number);
    }
}
