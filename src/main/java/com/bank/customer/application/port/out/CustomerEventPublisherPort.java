package com.bank.customer.application.port.out;

import com.bank.customer.domain.event.CustomerDomainEvent;
import io.reactivex.rxjava3.core.Completable;

public interface CustomerEventPublisherPort {

    Completable publish(CustomerDomainEvent event);
}
