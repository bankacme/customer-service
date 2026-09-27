package com.bank.customer.domain.event;

import com.bank.customer.domain.model.Customer;
import java.time.Instant;

/** Published after update() or changeProfile(). Carries the full current state, per the ficha. */
public record CustomerUpdated(
        String customerId,
        String type,
        String profile,
        String status,
        String name,
        DocumentSnapshot document,
        Instant occurredAt) implements CustomerDomainEvent {

    public static CustomerUpdated from(Customer customer) {
        return new CustomerUpdated(
                customer.id().value(),
                customer.type().name(),
                customer.profile().name(),
                customer.status().name(),
                customer.name().value(),
                new DocumentSnapshot(customer.document().type().name(), customer.document().number()),
                customer.updatedAt());
    }
}
