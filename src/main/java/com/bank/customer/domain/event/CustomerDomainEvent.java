package com.bank.customer.domain.event;

/** Marker for every event this aggregate can raise, so the publisher port stays typed. */
public sealed interface CustomerDomainEvent permits CustomerCreated, CustomerUpdated, CustomerDeleted {
}
