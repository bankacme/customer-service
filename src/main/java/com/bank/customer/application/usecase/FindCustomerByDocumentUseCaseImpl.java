package com.bank.customer.application.usecase;

import com.bank.customer.application.port.in.FindCustomerByDocumentUseCase;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.domain.exception.CustomerNotFoundException;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.DocumentType;
import io.reactivex.rxjava3.core.Single;

public class FindCustomerByDocumentUseCaseImpl implements FindCustomerByDocumentUseCase {

    private final CustomerRepositoryPort repositoryPort;

    public FindCustomerByDocumentUseCaseImpl(CustomerRepositoryPort repositoryPort) {
        this.repositoryPort = repositoryPort;
    }

    @Override
    public Single<Customer> execute(DocumentType type, String number) {
        return repositoryPort.findByDocument(type, number)
                .switchIfEmpty(Single.error(new CustomerNotFoundException(type + " " + number)));
    }
}
