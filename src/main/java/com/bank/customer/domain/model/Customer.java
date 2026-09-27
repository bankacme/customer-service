package com.bank.customer.domain.model;

import com.bank.customer.domain.exception.InvalidCustomerException;

import java.time.Clock;
import java.time.Instant;

public record Customer(
        CustomerId id,
        CustomerType type,
        CustomerProfile profile,
        CustomerName name,
        Document document,
        ContactInfo contact,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public Customer {
        if (id == null || type == null || profile == null || name == null
                || document == null || contact == null || status == null
                || createdAt == null || updatedAt == null) {
            throw new IllegalArgumentException("All Customer fields are required");
        }
    }

    public static Customer create(CustomerType type, CustomerProfile requestedProfile, CustomerName name,
                                  Document document, ContactInfo contact, Clock clock) {
        CustomerProfile profile = requestedProfile == null ? CustomerProfile.STANDARD : requestedProfile;
        requireDocumentTypeMatchesCustomerType(type, document.type());
        requireProfileCompatibleWithType(type, profile);
        Instant now = clock.instant();
        return new Customer(CustomerId.newId(), type, profile, name, document, contact,
                CustomerStatus.ACTIVE, now, now);
    }

    public Customer update(CustomerName newName, ContactInfo newContact, Clock clock) {
        requireActive();
        return new Customer(id, type, profile, newName, document, newContact, status, createdAt, clock.instant());
    }

    public Customer changeProfile(CustomerProfile newProfile, Clock clock) {
        requireActive();
        requireProfileCompatibleWithType(type, newProfile);
        return new Customer(id, type, newProfile, name, document, contact, status, createdAt, clock.instant());
    }

    public Customer deactivate(Clock clock) {
        if (status == CustomerStatus.INACTIVE) {
            return this;
        }
        return new Customer(id, type, profile, name, document, contact, CustomerStatus.INACTIVE, createdAt,
                clock.instant());
    }

    private void requireActive() {
        if (status == CustomerStatus.INACTIVE) {
            throw new InvalidCustomerException("CUSTOMER_INACTIVE", "Customer " + id.value() + " is inactive");
        }
    }

    private static void requireDocumentTypeMatchesCustomerType(CustomerType type, DocumentType documentType) {
        boolean valid = switch (type) {
            case PERSONAL -> documentType == DocumentType.DNI || documentType == DocumentType.CEX
                    || documentType == DocumentType.PASSPORT;
            case BUSINESS -> documentType == DocumentType.RUC;
        };
        if (!valid) {
            throw new InvalidCustomerException("DOCUMENT_TYPE_NOT_ALLOWED",
                    documentType + " is not allowed for a " + type + " customer");
        }
    }

    private static void requireProfileCompatibleWithType(CustomerType type, CustomerProfile profile) {
        boolean valid = switch (profile) {
            case STANDARD -> true;
            case VIP -> type == CustomerType.PERSONAL;
            case PYME -> type == CustomerType.BUSINESS;
        };
        if (!valid) {
            throw new InvalidCustomerException("PROFILE_NOT_ALLOWED",
                    profile + " is not allowed for a " + type + " customer");
        }
    }
}
