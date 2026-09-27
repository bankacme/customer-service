package com.bank.customer.domain.model;

public record ContactInfo(Email email, PhoneNumber phone, String address) {

    private static final int MAX_ADDRESS_LENGTH = 200;

    public ContactInfo {
        if (email == null || phone == null) {
            throw new IllegalArgumentException("Email and phone cannot be null");
        }

        if (address != null) {
            address = address.isBlank() ? null : address.trim();
            if (address != null && address.length() > MAX_ADDRESS_LENGTH) {
                throw new IllegalArgumentException("Address must be at most " + MAX_ADDRESS_LENGTH + " characters");
            }
        }
    }
}
