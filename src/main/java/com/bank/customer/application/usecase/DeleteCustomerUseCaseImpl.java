package com.bank.customer.application.usecase;

import com.bank.customer.application.port.in.DeleteCustomerUseCase;
import com.bank.customer.application.port.out.CustomerCachePort;
import com.bank.customer.application.port.out.CustomerEventPublisherPort;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.domain.event.CustomerDeleted;
import com.bank.customer.domain.exception.CustomerNotFoundException;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import com.bank.customer.domain.model.CustomerStatus;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import java.time.Clock;

public class DeleteCustomerUseCaseImpl implements DeleteCustomerUseCase {

    private final CustomerRepositoryPort repositoryPort;
    private final CustomerEventPublisherPort eventPublisherPort;
    private final CustomerCachePort cachePort;
    private final Clock clock;

    public DeleteCustomerUseCaseImpl(CustomerRepositoryPort repositoryPort,
                                      CustomerEventPublisherPort eventPublisherPort, CustomerCachePort cachePort,
                                      Clock clock) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.cachePort = cachePort;
        this.clock = clock;
    }

    @Override
    public Completable execute(CustomerId id) {
        return repositoryPort.findById(id)
                .switchIfEmpty(Single.error(new CustomerNotFoundException(id.value())))
                .map(this::deactivate)
                .flatMap(result -> repositoryPort.save(result.customer())
                        .map(saved -> new DeactivationResult(saved, result.wasActive())))
                .flatMapCompletable(result -> cachePort.evict(id)
                        .andThen(result.wasActive()
                                ? eventPublisherPort.publish(CustomerDeleted.from(result.customer()))
                                : Completable.complete()));
    }

    private DeactivationResult deactivate(Customer customer) {
        boolean wasActive = customer.status() == CustomerStatus.ACTIVE;
        return new DeactivationResult(customer.deactivate(clock), wasActive);
    }

    private record DeactivationResult(Customer customer, boolean wasActive) {
    }
}
