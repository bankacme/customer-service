package com.bank.customer.domain.model;

public record CustomerId(String value) {
    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId cannot be null or blank");
        }
    }

    public static CustomerId newId() {
        return new CustomerId(java.util.UUID.randomUUID().toString());
    }
}
