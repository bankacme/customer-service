package com.bank.customer.application.port.in;

import com.bank.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Flowable;

public interface FindAllCustomersUseCase {

    Flowable<Customer> execute(CustomerFilter filter);
}
