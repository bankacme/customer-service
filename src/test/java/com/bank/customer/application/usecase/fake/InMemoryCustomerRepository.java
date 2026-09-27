package com.bank.customer.application.usecase.fake;

import com.bank.customer.application.port.in.CustomerFilter;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import com.bank.customer.domain.model.DocumentType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.util.LinkedHashMap;
import java.util.Map;

/** Test double: a Map instead of Mongo. Mirrors the behaviour the real adapter (R4) must have. */
public class InMemoryCustomerRepository implements CustomerRepositoryPort {

    private final Map<String, Customer> byId = new LinkedHashMap<>();

    @Override
    public Single<Customer> save(Customer customer) {
        byId.put(customer.id().value(), customer);
        return Single.just(customer);
    }

    @Override
    public Maybe<Customer> findById(CustomerId id) {
        Customer found = byId.get(id.value());
        return found == null ? Maybe.empty() : Maybe.just(found);
    }

    @Override
    public Flowable<Customer> findAll(CustomerFilter filter) {
        return Flowable.fromIterable(byId.values())
                .filter(c -> filter.type() == null || c.type() == filter.type())
                .filter(c -> filter.profile() == null || c.profile() == filter.profile())
                .filter(c -> filter.status() == null || c.status() == filter.status());
    }

    @Override
    public Maybe<Customer> findByDocument(DocumentType type, String number) {
        return Flowable.fromIterable(byId.values())
                .filter(c -> c.document().type() == type && c.document().number().equals(number))
                .firstElement();
    }

    @Override
    public Single<Boolean> existsByDocument(DocumentType type, String number) {
        return findByDocument(type, number).isEmpty().map(empty -> !empty);
    }
}
