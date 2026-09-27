package com.bank.customer.infrastructure.config;

import com.bank.customer.application.port.out.CustomerCachePort;
import com.bank.customer.application.port.out.CustomerEventPublisherPort;
import com.bank.customer.application.port.out.CustomerRepositoryPort;
import com.bank.customer.application.usecase.ChangeCustomerProfileUseCaseImpl;
import com.bank.customer.application.usecase.CreateCustomerUseCaseImpl;
import com.bank.customer.application.usecase.DeleteCustomerUseCaseImpl;
import com.bank.customer.application.usecase.FindAllCustomersUseCaseImpl;
import com.bank.customer.application.usecase.FindCustomerByDocumentUseCaseImpl;
import com.bank.customer.application.usecase.FindCustomerByIdUseCaseImpl;
import com.bank.customer.application.usecase.UpdateCustomerUseCaseImpl;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the 7 use cases as beans here, instead of annotating the *Impl classes with
 * @Service — the application layer stays framework-agnostic (no Spring import in it),
 * same principle as the ports/domain being pure Java. Only this class knows about Spring.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public CreateCustomerUseCaseImpl createCustomerUseCase(CustomerRepositoryPort repositoryPort,
                                                             CustomerEventPublisherPort eventPublisherPort,
                                                             Clock clock) {
        return new CreateCustomerUseCaseImpl(repositoryPort, eventPublisherPort, clock);
    }

    @Bean
    public FindCustomerByIdUseCaseImpl findCustomerByIdUseCase(CustomerRepositoryPort repositoryPort,
                                                                 CustomerCachePort cachePort) {
        return new FindCustomerByIdUseCaseImpl(repositoryPort, cachePort);
    }

    @Bean
    public FindAllCustomersUseCaseImpl findAllCustomersUseCase(CustomerRepositoryPort repositoryPort) {
        return new FindAllCustomersUseCaseImpl(repositoryPort);
    }

    @Bean
    public FindCustomerByDocumentUseCaseImpl findCustomerByDocumentUseCase(CustomerRepositoryPort repositoryPort) {
        return new FindCustomerByDocumentUseCaseImpl(repositoryPort);
    }

    @Bean
    public UpdateCustomerUseCaseImpl updateCustomerUseCase(CustomerRepositoryPort repositoryPort,
                                                             CustomerEventPublisherPort eventPublisherPort,
                                                             CustomerCachePort cachePort, Clock clock) {
        return new UpdateCustomerUseCaseImpl(repositoryPort, eventPublisherPort, cachePort, clock);
    }

    @Bean
    public ChangeCustomerProfileUseCaseImpl changeCustomerProfileUseCase(CustomerRepositoryPort repositoryPort,
                                                                          CustomerEventPublisherPort eventPublisherPort,
                                                                          CustomerCachePort cachePort, Clock clock) {
        return new ChangeCustomerProfileUseCaseImpl(repositoryPort, eventPublisherPort, cachePort, clock);
    }

    @Bean
    public DeleteCustomerUseCaseImpl deleteCustomerUseCase(CustomerRepositoryPort repositoryPort,
                                                             CustomerEventPublisherPort eventPublisherPort,
                                                             CustomerCachePort cachePort, Clock clock) {
        return new DeleteCustomerUseCaseImpl(repositoryPort, eventPublisherPort, cachePort, clock);
    }
}
