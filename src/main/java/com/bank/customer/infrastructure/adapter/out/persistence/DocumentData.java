package com.bank.customer.infrastructure.adapter.out.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded identity document. Named {@code DocumentData}, not {@code Document} — that
 * name is already taken by {@code org.bson.Document} and by our own domain VO.
 * type/number kept as plain {@code String} (the enum's {@code name()}), same convention
 * used in bank-spike for status fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentData {

    private String type;

    private String number;
}
