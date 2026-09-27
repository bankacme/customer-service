package com.bank.customer.application.port.in;

import com.bank.customer.application.command.CreateCustomerCommand;
import com.bank.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Single;

public interface CreateCustomerUseCase {

    Single<Customer> execute(CreateCustomerCommand command);
}
