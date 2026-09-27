package com.bank.customer.infrastructure.adapter.out.persistence;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("customers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDocument {

    @Id
    private String id;

    private String type;

    private String profile;

    private String name;

    private DocumentData document;

    private ContactData contact;

    private String status;

    private Instant createdAt;

    private Instant updatedAt;
}
