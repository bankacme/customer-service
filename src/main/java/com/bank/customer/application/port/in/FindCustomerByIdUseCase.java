package com.bank.customer.application.port.in;

import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import io.reactivex.rxjava3.core.Single;

public interface FindCustomerByIdUseCase {

    Single<Customer> execute(CustomerId id);
}
