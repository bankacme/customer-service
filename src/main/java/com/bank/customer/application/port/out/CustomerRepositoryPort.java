package com.bank.customer.application.port.out;

import com.bank.customer.application.port.in.CustomerFilter;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import com.bank.customer.domain.model.DocumentType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public interface CustomerRepositoryPort {

    Single<Customer> save(Customer customer);

    Maybe<Customer> findById(CustomerId id);

    Flowable<Customer> findAll(CustomerFilter filter);

    Maybe<Customer> findByDocument(DocumentType type, String number);

    Single<Boolean> existsByDocument(DocumentType type, String number);
}
