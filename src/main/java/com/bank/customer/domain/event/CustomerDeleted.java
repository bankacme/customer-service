package com.bank.customer.domain.event;

import com.bank.customer.domain.model.Customer;
import java.time.Instant;

/** Published after deactivate(). Carries the full current state (now INACTIVE), per la ficha. */
public record CustomerDeleted(
        String customerId,
        String type,
        String profile,
        String status,
        String name,
        DocumentSnapshot document,
        Instant occurredAt) implements CustomerDomainEvent {

    public static CustomerDeleted from(Customer customer) {
        return new CustomerDeleted(
                customer.id().value(),
                customer.type().name(),
                customer.profile().name(),
                customer.status().name(),
                customer.name().value(),
                new DocumentSnapshot(customer.document().type().name(), customer.document().number()),
                customer.updatedAt());
    }
}
