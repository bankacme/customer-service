package com.bank.customer.application.usecase.fake;

import com.bank.customer.application.port.out.CustomerCachePort;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;

/** Mirrors the real P1/P2 adapter: always empty, never actually caches anything. */
public class NoOpCustomerCache implements CustomerCachePort {

    @Override
    public Maybe<Customer> get(CustomerId id) {
        return Maybe.empty();
    }

    @Override
    public Completable put(Customer customer) {
        return Completable.complete();
    }

    @Override
    public Completable evict(CustomerId id) {
        return Completable.complete();
    }
}
