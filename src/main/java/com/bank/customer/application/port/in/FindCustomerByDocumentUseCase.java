package com.bank.customer.application.port.in;

import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.DocumentType;
import io.reactivex.rxjava3.core.Single;

public interface FindCustomerByDocumentUseCase {

    Single<Customer> execute(DocumentType type, String number);
}
