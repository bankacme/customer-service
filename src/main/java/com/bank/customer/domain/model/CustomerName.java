package com.bank.customer.domain.model;

public record CustomerName(String value) {

    private static final int MAX_LENGTH = 150;

    public CustomerName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Customer name cannot be null or blank");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Customer name cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
