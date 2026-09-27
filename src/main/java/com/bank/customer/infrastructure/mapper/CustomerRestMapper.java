package com.bank.customer.infrastructure.mapper;

import com.bank.customer.application.command.CreateCustomerCommand;
import com.bank.customer.application.command.UpdateCustomerCommand;
import com.bank.customer.application.port.in.CustomerFilter;
import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerProfile;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import com.bank.customer.infrastructure.adapter.in.rest.dto.Contact;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Component;

/**
 * REST DTOs (generated from the contract) <-> application/domain types. Hand-written,
 * same reasoning as CustomerDocumentMapper: almost every field needs VO-unwrapping.
 */
@Component
public class CustomerRestMapper {

    public CreateCustomerCommand toCommand(
            com.bank.customer.infrastructure.adapter.in.rest.dto.CreateCustomerRequest request) {
        return new CreateCustomerCommand(
                CustomerType.valueOf(request.getType().name()),
                request.getProfile() == null ? null : CustomerProfile.valueOf(request.getProfile().name()),
                new CustomerName(request.getName()),
                toDocument(request.getDocument()),
                toContactInfo(request.getContact()));
    }

    public UpdateCustomerCommand toCommand(
            com.bank.customer.infrastructure.adapter.in.rest.dto.UpdateCustomerRequest request) {
        return new UpdateCustomerCommand(new CustomerName(request.getName()), toContactInfo(request.getContact()));
    }

    public CustomerProfile toProfile(
            com.bank.customer.infrastructure.adapter.in.rest.dto.ChangeProfileRequest request) {
        return CustomerProfile.valueOf(request.getProfile().name());
    }

    public DocumentType toDocumentType(com.bank.customer.infrastructure.adapter.in.rest.dto.DocumentType type) {
        return DocumentType.valueOf(type.name());
    }

    public CustomerFilter toFilter(com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerType type,
                                    com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerProfile profile,
                                    com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerStatus status) {
        return new CustomerFilter(
                type == null ? null : CustomerType.valueOf(type.name()),
                profile == null ? null : CustomerProfile.valueOf(profile.name()),
                status == null ? null : com.bank.customer.domain.model.CustomerStatus.valueOf(status.name()));
    }

    public com.bank.customer.infrastructure.adapter.in.rest.dto.Customer toDto(
            com.bank.customer.domain.model.Customer customer) {
        com.bank.customer.infrastructure.adapter.in.rest.dto.Customer dto =
                new com.bank.customer.infrastructure.adapter.in.rest.dto.Customer();
        dto.setId(customer.id().value());
        dto.setType(com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerType
                .valueOf(customer.type().name()));
        dto.setProfile(com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerProfile
                .valueOf(customer.profile().name()));
        dto.setName(customer.name().value());
        dto.setDocument(toDocumentDto(customer.document()));
        dto.setContact(toContactDto(customer.contact()));
        dto.setStatus(com.bank.customer.infrastructure.adapter.in.rest.dto.CustomerStatus
                .valueOf(customer.status().name()));
        dto.setCreatedAt(toOffsetDateTime(customer.createdAt()));
        dto.setUpdatedAt(toOffsetDateTime(customer.updatedAt()));
        return dto;
    }

    private Document toDocument(com.bank.customer.infrastructure.adapter.in.rest.dto.Document dto) {
        return new Document(DocumentType.valueOf(dto.getType().name()), dto.getNumber());
    }

    private ContactInfo toContactInfo(Contact dto) {
        return new ContactInfo(new Email(dto.getEmail()), new PhoneNumber(dto.getPhone()), dto.getAddress());
    }

    private com.bank.customer.infrastructure.adapter.in.rest.dto.Document toDocumentDto(Document document) {
        com.bank.customer.infrastructure.adapter.in.rest.dto.Document dto =
                new com.bank.customer.infrastructure.adapter.in.rest.dto.Document();
        dto.setType(com.bank.customer.infrastructure.adapter.in.rest.dto.DocumentType
                .valueOf(document.type().name()));
        dto.setNumber(document.number());
        return dto;
    }

    private Contact toContactDto(ContactInfo contact) {
        Contact dto = new Contact();
        dto.setEmail(contact.email().value());
        dto.setPhone(contact.phone().value());
        dto.setAddress(contact.address());
        return dto;
    }

    private OffsetDateTime toOffsetDateTime(Instant instant) {
        return instant.atOffset(ZoneOffset.UTC);
    }
}
