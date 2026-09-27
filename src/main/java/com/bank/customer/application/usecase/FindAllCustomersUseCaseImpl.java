package com.bank.customer.application.usecase;

import com.bank.customer.application.port.in.CustomerFilter;
import com.bank.customer.application.port.in.FindAllCustomersUseCase;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Flowable;

public class FindAllCustomersUseCaseImpl implements FindAllCustomersUseCase {

    private final CustomerRepositoryPort repositoryPort;

    public FindAllCustomersUseCaseImpl(CustomerRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Flowable<Customer> execute(CustomerFilter filter) {
        return repositoryPort.findAll(filter);
    }
}
