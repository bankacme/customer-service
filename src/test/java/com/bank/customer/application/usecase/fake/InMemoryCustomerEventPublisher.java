package com.bank.customer.application.usecase.fake;

import com.bank.customer.application.port.out.CustomerEventPublisherPort;
import com.bank.customer.domain.event.CustomerDomainEvent;
import io.reactivex.rxjava3.core.Completable;
import java.util.ArrayList;
import java.util.List;

public class InMemoryCustomerEventPublisher implements CustomerEventPublisherPort {

    private final List<CustomerDomainEvent> published = new ArrayList<>();

    @Override
    public Completable publish(CustomerDomainEvent event) {
        return Completable.fromAction(() -> published.add(event));
    }

    public List<CustomerDomainEvent> published() {
        return published;
    }
}
