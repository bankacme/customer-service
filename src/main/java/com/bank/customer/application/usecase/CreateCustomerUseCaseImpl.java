package com.bank.customer.application.usecase;

import com.bank.customer.application.command.CreateCustomerCommand;
import com.bank.customer.application.port.in.CreateCustomerUseCase;
import com.bank.customer.application.port.out.CustomerEventPublisherPort;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.domain.event.CustomerCreated;
import com.bank.customer.domain.exception.DuplicateDocumentException;
import com.bank.customer.domain.model.Customer;
import io.reactivex.rxjava3.core.Single;
import java.time.Clock;

public class CreateCustomerUseCaseImpl implements CreateCustomerUseCase {

    private final CustomerRepositoryPort repositoryPort;
    private final CustomerEventPublisherPort eventPublisherPort;
    private final Clock clock;

    public CreateCustomerUseCaseImpl(CustomerRepositoryPort repositoryPort,
                                      CustomerEventPublisherPort eventPublisherPort, Clock clock) {
        this.repositoryPort = repositoryPort;
        this.eventPublisherPort = eventPublisherPort;
        this.clock = clock;
    }

    @Override
    public Single<Customer> execute(CreateCustomerCommand command) {
        String type = command.document().type().name();
        String number = command.document().number();

        return repositoryPort.existsByDocument(command.document().type(), number)
                .flatMap(exists -> exists
                        ? Single.<Customer>error(new DuplicateDocumentException(type, number))
                        : Single.fromCallable(() -> Customer.create(command.type(), command.profile(),
                                command.name(), command.document(), command.contact(), clock)))
                .flatMap(repositoryPort::save)
                .flatMap(saved -> eventPublisherPort.publish(CustomerCreated.from(saved))
                        .andThen(Single.just(saved)));
    }
}
