package com.bank.customer.application.port.out;

import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

public interface CustomerCachePort {

    Maybe<Customer> get(CustomerId id);

    Completable put(Customer customer);

    Completable evict(CustomerId id);
}
