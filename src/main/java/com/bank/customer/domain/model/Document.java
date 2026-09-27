package com.bank.customer.domain.model;

import com.bank.customer.domain.exception.InvalidCustomerException;

import java.util.regex.Pattern;

public record Document(DocumentType type, String number) {

    private static final Pattern DNI = Pattern.compile("^\\d{8}$");
    private static final Pattern CEX = Pattern.compile("^[A-Za-z0-9]{9,12}$");
    private static final Pattern PASSPORT = Pattern.compile("^[A-Za-z0-9]{6,12}$");
    private static final Pattern RUC = Pattern.compile("^\\d{11}$");

    public Document {
        if (type == null) {
            throw new IllegalArgumentException("Document type cannot be null");
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Document number cannot be null or blank");
        }

        if (!formatFor(type).matcher(number).matches()) {
            throw new InvalidCustomerException("INVALID_DOCUMENT",
                    "Document number does not match the format for " + type);
        }
    }

    private static Pattern formatFor(DocumentType type) {
        return switch (type) {
            case DNI -> DNI;
            case CEX -> CEX;
            case PASSPORT -> PASSPORT;
            case RUC -> RUC;
        };
    }
}
