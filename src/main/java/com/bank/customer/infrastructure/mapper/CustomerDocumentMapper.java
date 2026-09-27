package com.bank.customer.infrastructure.mapper;

import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.Customer;
import com.bank.customer.domain.model.CustomerId;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerProfile;
import com.bank.customer.domain.model.CustomerStatus;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;
import com.bank.customer.domain.model.DocumentType;
import com.bank.customer.domain.model.Email;
import com.bank.customer.domain.model.PhoneNumber;
import com.bank.customer.infrastructure.adapter.out.persistence.ContactData;
import com.bank.customer.infrastructure.adapter.out.persistence.CustomerDocument;
import com.bank.customer.infrastructure.adapter.out.persistence.DocumentData;
import org.springframework.stereotype.Component;

@Component
public class CustomerDocumentMapper {

    public CustomerDocument toDocument(Customer customer) {
        return CustomerDocument.builder()
                .id(customer.id().value())
                .type(customer.type().name())
                .profile(customer.profile().name())
                .name(customer.name().value())
                .document(DocumentData.builder()
                        .type(customer.document().type().name())
                        .number(customer.document().number())
                        .build())
                .contact(ContactData.builder()
                        .email(customer.contact().email().value())
                        .phone(customer.contact().phone().value())
                        .address(customer.contact().address())
                        .build())
                .status(customer.status().name())
                .createdAt(customer.createdAt())
                .updatedAt(customer.updatedAt())
                .build();
    }

    public Customer toDomain(CustomerDocument document) {
        return new Customer(
                new CustomerId(document.getId()),
                CustomerType.valueOf(document.getType()),
                CustomerProfile.valueOf(document.getProfile()),
                new CustomerName(document.getName()),
                new Document(
                        DocumentType.valueOf(document.getDocument().getType()),
                        document.getDocument().getNumber()),
                new ContactInfo(
                        new Email(document.getContact().getEmail()),
                        new PhoneNumber(document.getContact().getPhone()),
                        document.getContact().getAddress()),
                CustomerStatus.valueOf(document.getStatus()),
                document.getCreatedAt(),
                document.getUpdatedAt());
    }
}
