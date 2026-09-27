package com.bank.customer.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final int MAX_LENGTH = 254;
    private static final Pattern FORMAT = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }

        value = value.trim().toLowerCase(Locale.ROOT);

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Email cannot exceed " + MAX_LENGTH + " characters");
        }

        if (!value.matches(FORMAT.pattern())) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
}
