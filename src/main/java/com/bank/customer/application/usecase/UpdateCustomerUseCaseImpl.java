package com.bank.customer.application.usecase;

import com.bank.customer.application.command.UpdateCustomerCommand;
import com.bank.customer.application.port.in.UpdateCustomerUseCase;
import com.bank.customer.application.port.out.CustomerCachePort;
import com.bank.customer.application.port.out.CustomerEventPublisherPort;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.domain.event.CustomerUpdated;
import com.bank.customer.domain.exception.CustomerNotFoundException;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import io.reactivex.rxjava3.core.Single;
import java.time.Clock;

public class UpdateCustomerUseCaseImpl implements UpdateCustomerUseCase {

    private final CustomerRepositoryPort repositoryPort;
    private final CustomerEventPublisherPort eventPublisherPort;
    private final CustomerCachePort cachePort;
    private final Clock clock;

    public UpdateCustomerUseCaseImpl(CustomerRepositoryPort repositoryPort,
                                      CustomerEventPublisherPort eventPublisherPort, CustomerCachePort cachePort,
                                      Clock clock) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.cachePort = cachePort;
        this.clock = clock;
    }

    @Override
    public Single<Customer> execute(CustomerId id, UpdateCustomerCommand command) {
        return repositoryPort.findById(id)
                .switchIfEmpty(Single.error(new CustomerNotFoundException(id.value())))
                .map(customer -> customer.update(command.name(), command.contact(), clock))
                .flatMap(repositoryPort::save)
                .flatMap(saved -> cachePort.put(saved)
                        .andThen(eventPublisherPort.publish(CustomerUpdated.from(saved)))
                        .andThen(Single.just(saved)));
    }
}
