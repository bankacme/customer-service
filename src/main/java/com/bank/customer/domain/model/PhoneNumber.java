package com.bank.customer.domain.model;

import java.util.regex.Pattern;

public record PhoneNumber(String value) {

    private static final int MAX_LENGTH = 15;
    private static final Pattern FORMAT = Pattern.compile("^9\\d{8}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be null or blank");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Phone number cannot exceed " + MAX_LENGTH + " characters");
        }

        if (!FORMAT.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }
}
