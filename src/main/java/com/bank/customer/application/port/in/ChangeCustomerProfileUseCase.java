package com.bank.customer.application.port.in;

import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import com.bank.customer.domain.model.CustomerProfile;
import io.reactivex.rxjava3.core.Single;

public interface ChangeCustomerProfileUseCase {

    Single<Customer> execute(CustomerId id, CustomerProfile newProfile);
}
