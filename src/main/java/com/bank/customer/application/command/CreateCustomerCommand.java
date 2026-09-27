package com.bank.customer.application.command;

import com.bank.customer.domain.model.ContactInfo;
import com.bank.customer.domain.model.CustomerName;
import com.bank.customer.domain.model.CustomerProfile;
import com.bank.customer.domain.model.CustomerType;
import com.bank.customer.domain.model.Document;

public record CreateCustomerCommand(
        CustomerType type,
        CustomerProfile profile,
        CustomerName name,
        Document document,
        ContactInfo contact) {
}
