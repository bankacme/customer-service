package com.bank.customer.domain.event;

import com.bank.customer.domain.model.Customer;
import java.time.Instant;

/** Published after a customer is created. Carries the full current state, per the ficha. */
public record CustomerCreated(
        String customerId,
        String type,
        String profile,
        String status,
        String name,
        DocumentSnapshot document,
        Instant occurredAt) implements CustomerDomainEvent {

    public static CustomerCreated from(Customer customer) {
        return new CustomerCreated(
                customer.id().value(),
                customer.type().name(),
                customer.profile().name(),
                customer.status().name(),
                customer.name().value(),
                new DocumentSnapshot(customer.document().type().name(), customer.document().number()),
                customer.updatedAt());
    }
}
