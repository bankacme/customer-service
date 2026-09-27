package com.bank.customer.infrastructure.adapter.out.cache;

import com.bank.customer.application.port.out.CustomerCachePort;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import org.springframework.stereotype.Component;

/** No-op until P3 (Redis). Always misses, so callers behave exactly as if there were no cache. */
@Component
public class CustomerRedisCacheAdapter implements CustomerCachePort {

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
