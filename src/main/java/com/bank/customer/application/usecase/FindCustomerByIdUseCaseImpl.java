package com.bank.customer.application.usecase;

import com.bank.customer.application.port.in.FindCustomerByIdUseCase;
import com.bank.customer.application.port.out.CustomerCachePort;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.domain.exception.CustomerNotFoundException;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public class FindCustomerByIdUseCaseImpl implements FindCustomerByIdUseCase {

    private final CustomerRepositoryPort repositoryPort;
    private final CustomerCachePort cachePort;

    public FindCustomerByIdUseCaseImpl(CustomerRepositoryPort repositoryPort, CustomerCachePort cachePort) {
        this.repositoryPort = repositoryPort;
        this.cachePort = cachePort;
    }

    @Override
    public Single<Customer> execute(CustomerId id) {
        return cachePort.get(id)
                .switchIfEmpty(fromRepositoryAndPopulateCache(id))
                .switchIfEmpty(Single.error(new CustomerNotFoundException(id.value())));
    }

    private Maybe<Customer> fromRepositoryAndPopulateCache(CustomerId id) {
        return repositoryPort.findById(id)
                .flatMap(customer -> cachePort.put(customer).andThen(Maybe.just(customer)));
    }
}
