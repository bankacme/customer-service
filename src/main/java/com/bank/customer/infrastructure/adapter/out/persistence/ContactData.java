package com.bank.customer.infrastructure.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactData {

    private String email;

    private String phone;

    /** Omitted from the JSON in Mongo when null (no {@code @Field} needed for that; Mongo just skips nulls). */
    private String address;
}
